package com.winnguyen1905.activity.websocket.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
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
    private final SocketCacheService socketCacheService;
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

        // Check local connection first
        SocketIOClient client = userSocketMap.get(userId);
        boolean locallyConnected = client != null && client.isChannelOpen();
        
        if (!locallyConnected && client != null) {
            // Clean up stale connection
            removeUserFromMap(userId);
        }
        
        // Also check Redis cache for distributed deployment support
        boolean cacheConnected = socketCacheService.isUserOnline(userId);
        
        // If user is in cache but not locally connected, they might be connected to another instance
        if (!locallyConnected && cacheConnected) {
            log.debug("User {} is online in cache but not locally connected (distributed deployment)", userId);
        }
        
        return locallyConnected || cacheConnected;
    }

    /**
     * Gets the current number of connected users.
     *
     * @return the number of connected users
     */
    public int getConnectedUserCount() {
        // Clean up any stale connections before counting
        cleanupStaleConnections();
        
        // Use Redis cache for distributed count
        Set<Long> onlineUsers = socketCacheService.getOnlineUsers();
        return onlineUsers.size();
    }

    /**
     * Gets a copy of currently connected user IDs.
     *
     * @return Set of connected user IDs
     */
    public Set<Long> getConnectedUserIds() {
        cleanupStaleConnections();
        
        // Use Redis cache for distributed user list
        return socketCacheService.getOnlineUsers();
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
     * @param client The connected SocketIO client
     */
    private void handleClientConnection(SocketIOClient client) {
        try {
            String userIdParam = extractUserIdFromHandshake(client);
            String sessionId = client.getSessionId().toString();
            
            if (userIdParam != null) {
                registerUserConnection(userIdParam, client, sessionId);
            } else {
                log.warn("Client connected without valid user ID: {}", sessionId);
                client.disconnect();
            }
        } catch (Exception e) {
            log.error("Error handling client connection: {}", e.getMessage());
            client.disconnect();
        }
    }

    /**
     * Handles client disconnections.
     * 
     * @param client The disconnected SocketIO client
     */
    private void handleClientDisconnection(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        
        try {
            removeUserBySessionId(sessionId);
            log.debug(CONNECTION_LOG_FORMAT, sessionId, "disconnected");
        } catch (Exception e) {
            log.error("Error handling client disconnection: {}", e.getMessage());
        }
    }

    /**
     * Extracts user ID from client handshake data.
     * 
     * @param client The SocketIO client
     * @return User ID as string or null if not found/invalid
     */
    private String extractUserIdFromHandshake(SocketIOClient client) {
        try {
            return client.getHandshakeData().getSingleUrlParam(USER_ID_PARAM);
        } catch (Exception e) {
            log.warn("Failed to extract user ID from handshake: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Registers a new user connection in maps and cache.
     * 
     * @param userIdParam User ID as string
     * @param client SocketIO client
     * @param sessionId Session ID
     */
    private void registerUserConnection(String userIdParam, SocketIOClient client, String sessionId) {
        try {
            Long userId = Long.valueOf(userIdParam);
            
            // Store in local map
            userSocketMap.put(userId, client);
            
            // Store in distributed cache
            socketCacheService.addOnlineUser(userId, sessionId);
            socketCacheService.mapSessionToUser(sessionId, userId);
            
            log.info(CONNECTION_LOG_FORMAT, sessionId, "connected as user " + userId);
            
            // Send welcome notification
            client.sendEvent("connection_established", Map.of(
                    "userId", userId,
                    "timestamp", System.currentTimeMillis(),
                    "message", "Connected successfully"
            ));
            
        } catch (NumberFormatException e) {
            log.warn(INVALID_USER_ID_FORMAT, userIdParam);
            client.disconnect();
        }
    }

    /**
     * Removes user from connection map and cache.
     * 
     * @param userId User ID to remove
     */
    private void removeUserFromMap(Long userId) {
        if (userId != null) {
            userSocketMap.remove(userId);
            socketCacheService.removeOnlineUser(userId);
            
            // Also remove session mapping if exists
            String sessionId = socketCacheService.getUserSession(userId);
            if (sessionId != null) {
                socketCacheService.removeSessionMapping(sessionId);
            }
        }
    }

    /**
     * Removes user by session ID from connection map and cache.
     * 
     * @param sessionId Session ID to remove
     */
    private void removeUserBySessionId(String sessionId) {
        try {
            Long userId = socketCacheService.getUserIdBySession(sessionId);
            if (userId != null) {
                userSocketMap.remove(userId);
                socketCacheService.removeOnlineUser(userId);
                socketCacheService.removeSessionMapping(sessionId);
                log.debug("Removed user {} (session: {})", userId, sessionId);
            } else {
                // Try to find and remove from local map if cache lookup fails
                userSocketMap.entrySet().removeIf(entry -> {
                    SocketIOClient client = entry.getValue();
                    return client.getSessionId().toString().equals(sessionId);
                });
            }
        } catch (Exception e) {
            log.error("Error removing user by session ID {}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * Validates notification request parameters.
     * 
     * @param userId User ID
     * @param eventName Event name
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
     * @param eventName Event name to validate
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
     * Cleans up stale connections from the user socket map.
     */
    private void cleanupStaleConnections() {
        userSocketMap.entrySet().removeIf(entry -> {
            SocketIOClient client = entry.getValue();
            boolean isStale = client == null || !client.isChannelOpen();
            if (isStale) {
                log.debug("Cleaning up stale connection for user {}", entry.getKey());
                socketCacheService.removeOnlineUser(entry.getKey());
            }
            return isStale;
        });
    }
} 
