package com.winnguyen1905.activity.rest.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling Socket.IO connections and managing real-time notifications.
 * Provides functionality for user connection management, targeted messaging,
 * and broadcast notifications with proper error handling and logging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocketIOService {

    private final SocketIOServer socketIOServer;
    private final Map<Long, SocketIOClient> userSocketMap = new ConcurrentHashMap<>();

    // Configuration constants
    private static final String USER_ID_PARAM = "userId";
    private static final String CONNECTION_LOG_FORMAT = "Client {}: {}";
    private static final String NOTIFICATION_LOG_FORMAT = "Sent {} notification to user {}";
    private static final String BROADCAST_LOG_FORMAT = "Broadcast {} notification sent to {} connected users";
    
    // Error messages
    private static final String INVALID_USER_ID_FORMAT = "Invalid user ID format: {}";
    private static final String USER_NOT_CONNECTED = "User {} not connected, notification not sent";

    /**
     * Initializes the Socket.IO server and sets up connection handlers.
     * Called automatically after bean construction.
     */
    @PostConstruct
    private void initializeServer() {
        log.info("Initializing Socket.IO server...");
        
        setupConnectionHandlers();
        startServer();
        
        log.info("Socket.IO server initialized successfully on port {}", 
                 socketIOServer.getConfiguration().getPort());
    }

    /**
     * Gracefully shuts down the Socket.IO server.
     * Called automatically before bean destruction.
     */
    @PreDestroy
    private void shutdownServer() {
        if (socketIOServer != null) {
            log.info("Shutting down Socket.IO server...");
            socketIOServer.stop();
            userSocketMap.clear();
            log.info("Socket.IO server shut down successfully");
        }
    }

    /**
     * Sends a notification to a specific user.
     *
     * @param userId The user ID to send the notification to
     * @param eventName The event name to send
     * @param data The notification data to send
     * @return true if the notification was sent successfully, false otherwise
     */
    public boolean sendNotification(Long userId, String eventName, Object data) {
        if (!isValidNotificationRequest(userId, eventName)) {
            return false;
        }

        SocketIOClient client = userSocketMap.get(userId);
        if (client != null && client.isChannelOpen()) {
            try {
                client.sendEvent(eventName, data);
                log.debug(NOTIFICATION_LOG_FORMAT, eventName, userId);
                return true;
            } catch (Exception e) {
                log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
                // Remove disconnected client
                removeUserFromMap(userId);
                return false;
            }
        } else {
            log.debug(USER_NOT_CONNECTED, userId);
            // Clean up stale connection if exists
            if (client != null) {
                removeUserFromMap(userId);
            }
            return false;
        }
    }

    /**
     * Sends a notification to all connected users.
     *
     * @param eventName The event name to send
     * @param data The notification data to send
     * @return the number of connected users the notification was sent to
     */
    public int broadcastNotification(String eventName, Object data) {
        if (!isValidEventName(eventName)) {
            return 0;
        }

        try {
            int connectedUsers = getConnectedUserCount();
            socketIOServer.getBroadcastOperations().sendEvent(eventName, data);
            log.info(BROADCAST_LOG_FORMAT, eventName, connectedUsers);
            return connectedUsers;
        } catch (Exception e) {
            log.error("Failed to broadcast notification {}: {}", eventName, e.getMessage());
            return 0;
        }
    }

    /**
     * Checks if a user is currently connected and has an active session.
     *
     * @param userId The user ID to check
     * @return true if the user is connected with an active session, false otherwise
     */
    public boolean isUserConnected(Long userId) {
        if (userId == null) {
            return false;
        }

        SocketIOClient client = userSocketMap.get(userId);
        if (client != null && client.isChannelOpen()) {
            return true;
        } else if (client != null) {
            // Clean up stale connection
            removeUserFromMap(userId);
        }
        
        return false;
    }

    /**
     * Gets the current number of connected users.
     *
     * @return the number of connected users
     */
    public int getConnectedUserCount() {
        // Clean up any stale connections before counting
        cleanupStaleConnections();
        return userSocketMap.size();
    }

    /**
     * Gets a copy of currently connected user IDs.
     *
     * @return Set of connected user IDs
     */
    public java.util.Set<Long> getConnectedUserIds() {
        cleanupStaleConnections();
        return java.util.Set.copyOf(userSocketMap.keySet());
    }

    /**
     * Sets up connection and disconnection event handlers.
     */
    private void setupConnectionHandlers() {
        socketIOServer.addConnectListener(this::handleClientConnection);
        socketIOServer.addDisconnectListener(this::handleClientDisconnection);
    }

    /**
     * Starts the Socket.IO server.
     */
    private void startServer() {
        try {
            socketIOServer.start();
            log.info("Socket.IO server started successfully");
        } catch (Exception e) {
            log.error("Failed to start Socket.IO server: {}", e.getMessage());
            throw new RuntimeException("Socket.IO server startup failed", e);
        }
    }

    /**
     * Handles new client connections.
     *
     * @param client The connected client
     */
    private void handleClientConnection(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        log.debug(CONNECTION_LOG_FORMAT, "connected", sessionId);

        String userIdParam = extractUserIdFromHandshake(client);
        if (userIdParam != null) {
            registerUserConnection(userIdParam, client, sessionId);
        } else {
            log.warn("Client connected without user ID: {}", sessionId);
        }
    }

    /**
     * Handles client disconnections.
     *
     * @param client The disconnected client
     */
    private void handleClientDisconnection(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        log.debug(CONNECTION_LOG_FORMAT, "disconnected", sessionId);
        
        removeUserBySessionId(sessionId);
    }

    /**
     * Extracts user ID from client handshake data.
     *
     * @param client The client to extract user ID from
     * @return User ID parameter or null if not found/invalid
     */
    private String extractUserIdFromHandshake(SocketIOClient client) {
        return client.getHandshakeData().getSingleUrlParam(USER_ID_PARAM);
    }

    /**
     * Registers a user connection in the user-socket mapping.
     *
     * @param userIdParam The user ID parameter from handshake
     * @param client The connected client
     * @param sessionId The session ID for logging
     */
    private void registerUserConnection(String userIdParam, SocketIOClient client, String sessionId) {
        try {
            Long userId = Long.parseLong(userIdParam);
            
            // Remove any existing connection for this user
            SocketIOClient existingClient = userSocketMap.put(userId, client);
            if (existingClient != null && existingClient.isChannelOpen()) {
                log.debug("Replacing existing connection for user {}", userId);
                existingClient.disconnect();
            }
            
            log.info("User {} connected with session {}", userId, sessionId);
        } catch (NumberFormatException e) {
            log.warn(INVALID_USER_ID_FORMAT, userIdParam);
        }
    }

    /**
     * Removes a user from the mapping by user ID.
     *
     * @param userId The user ID to remove
     */
    private void removeUserFromMap(Long userId) {
        SocketIOClient removed = userSocketMap.remove(userId);
        if (removed != null) {
            log.debug("Removed user {} from connection map", userId);
        }
    }

    /**
     * Removes a user from the mapping by session ID.
     *
     * @param sessionId The session ID to find and remove
     */
    private void removeUserBySessionId(String sessionId) {
        userSocketMap.entrySet().removeIf(entry -> {
            boolean matches = entry.getValue().getSessionId().toString().equals(sessionId);
            if (matches) {
                log.debug("Removed user {} with session {}", entry.getKey(), sessionId);
            }
            return matches;
        });
    }

    /**
     * Validates notification request parameters.
     *
     * @param userId The user ID to validate
     * @param eventName The event name to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidNotificationRequest(Long userId, String eventName) {
        if (userId == null) {
            log.warn("Attempted to send notification with null user ID");
            return false;
        }
        
        return isValidEventName(eventName);
    }

    /**
     * Validates event name.
     *
     * @param eventName The event name to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEventName(String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            log.warn("Attempted to send notification with null or empty event name");
            return false;
        }
        return true;
    }

    /**
     * Removes stale connections from the user mapping.
     */
    private void cleanupStaleConnections() {
        userSocketMap.entrySet().removeIf(entry -> {
            boolean isStale = !entry.getValue().isChannelOpen();
            if (isStale) {
                log.debug("Cleaned up stale connection for user {}", entry.getKey());
            }
            return isStale;
        });
    }
}
