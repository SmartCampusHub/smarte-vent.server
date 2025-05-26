package com.winnguyen1905.Activity.rest.service;

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
 * Service for handling Socket.IO connections and sending real-time notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocketIOService {

    private final SocketIOServer socketIOServer;
    private final Map<Long, SocketIOClient> userSocketMap = new ConcurrentHashMap<>();

    /**
     * Start the Socket.IO server after initialization.
     */
    @PostConstruct
    private void startServer() {
        socketIOServer.addConnectListener(client -> {
            String sessionId = client.getSessionId().toString();
            log.info("Client connected: {}", sessionId);

            // Extract user ID from handshake data
            String userId = client.getHandshakeData().getSingleUrlParam("userId");
            if (userId != null && !userId.isEmpty()) {
                try {
                    Long userIdLong = Long.parseLong(userId);
                    userSocketMap.put(userIdLong, client);
                    log.info("User {} mapped to session {}", userIdLong, sessionId);
                } catch (NumberFormatException e) {
                    log.error("Invalid user ID format: {}", userId);
                }
            }
        });

        socketIOServer.addDisconnectListener(client -> {
            String sessionId = client.getSessionId().toString();
            log.info("Client disconnected: {}", sessionId);
            
            // Remove user from the map
            userSocketMap.entrySet().removeIf(entry -> 
                entry.getValue().getSessionId().toString().equals(sessionId));
        });

        socketIOServer.start();
        log.info("Socket.IO server started on port {}", socketIOServer.getConfiguration().getPort());
    }

    /**
     * Stop the Socket.IO server before shutdown.
     */
    @PreDestroy
    private void stopServer() {
        if (socketIOServer != null) {
            socketIOServer.stop();
            log.info("Socket.IO server stopped");
        }
    }

    /**
     * Send a notification to a specific user.
     *
     * @param userId The user ID to send the notification to
     * @param eventName The event name to send
     * @param data The notification data to send
     */
    public void sendNotification(Long userId, String eventName, Object data) {
        if (userId != null && userSocketMap.containsKey(userId)) {
            SocketIOClient client = userSocketMap.get(userId);
            client.sendEvent(eventName, data);
            log.info("Sent {} notification to user {}", eventName, userId);
        } else {
            log.debug("User {} not connected, notification not sent", userId);
        }
    }

    /**
     * Send a notification to all connected users.
     *
     * @param eventName The event name to send
     * @param data The notification data to send
     */
    public void broadcastNotification(String eventName, Object data) {
        socketIOServer.getBroadcastOperations().sendEvent(eventName, data);
        log.info("Broadcast {} notification sent", eventName);
    }

    /**
     * Check if a user is currently connected.
     *
     * @param userId The user ID to check
     * @return true if the user is connected, false otherwise
     */
    public boolean isUserConnected(Long userId) {
        return userSocketMap.containsKey(userId);
    }
}
