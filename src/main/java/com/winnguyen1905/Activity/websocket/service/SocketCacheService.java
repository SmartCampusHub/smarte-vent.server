package com.winnguyen1905.activity.websocket.service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.winnguyen1905.activity.websocket.dto.UserStatusDto;

/**
 * Service interface for managing SocketIO-related cache operations using Redis.
 * Provides methods for user session management, status tracking, and typing indicators.
 */
public interface SocketCacheService {

    // ==================== USER STATUS MANAGEMENT ====================
    
    /**
     * Sets user status in cache with expiration.
     *
     * @param userId The user ID
     * @param status The user status
     */
    void setUserStatus(Long userId, UserStatusDto.UserStatus status);

    /**
     * Gets user status from cache.
     *
     * @param userId The user ID
     * @return The user status or OFFLINE if not found
     */
    UserStatusDto.UserStatus getUserStatus(Long userId);

    /**
     * Removes user status from cache.
     *
     * @param userId The user ID
     */
    void removeUserStatus(Long userId);

    // ==================== LAST SEEN TRACKING ====================
    
    /**
     * Updates user's last seen timestamp.
     *
     * @param userId The user ID
     * @param lastSeen The last seen timestamp
     */
    void updateLastSeen(Long userId, Instant lastSeen);

    /**
     * Gets user's last seen timestamp.
     *
     * @param userId The user ID
     * @return The last seen timestamp or null if not found
     */
    Instant getLastSeen(Long userId);

    /**
     * Removes user's last seen data.
     *
     * @param userId The user ID
     */
    void removeLastSeen(Long userId);

    // ==================== TYPING INDICATORS ====================
    
    /**
     * Sets user as typing in a conversation.
     *
     * @param sessionId The session ID
     * @param userId The user ID
     * @param conversationId The conversation ID (activity ID or private chat partner ID)
     * @param isPrivate Whether this is a private conversation
     */
    void setUserTyping(String sessionId, Long userId, Long conversationId, boolean isPrivate);

    /**
     * Removes user from typing indicators.
     *
     * @param sessionId The session ID
     */
    void removeUserTyping(String sessionId);

    /**
     * Gets all users currently typing in a conversation.
     *
     * @param conversationId The conversation ID
     * @param isPrivate Whether this is a private conversation
     * @return Set of user IDs currently typing
     */
    Set<Long> getTypingUsers(Long conversationId, boolean isPrivate);

    /**
     * Gets user ID associated with a session.
     *
     * @param sessionId The session ID
     * @return The user ID or null if not found
     */
    Long getUserIdBySession(String sessionId);

    // ==================== ONLINE USERS TRACKING ====================
    
    /**
     * Adds user to online users set.
     *
     * @param userId The user ID
     * @param sessionId The session ID
     */
    void addOnlineUser(Long userId, String sessionId);

    /**
     * Removes user from online users set.
     *
     * @param userId The user ID
     */
    void removeOnlineUser(Long userId);

    /**
     * Gets all currently online users.
     *
     * @return Set of online user IDs
     */
    Set<Long> getOnlineUsers();

    /**
     * Checks if a user is currently online.
     *
     * @param userId The user ID
     * @return true if user is online, false otherwise
     */
    boolean isUserOnline(Long userId);

    // ==================== ACTIVITY PARTICIPANTS CACHE ====================
    
    /**
     * Caches activity participants for quick access.
     *
     * @param activityId The activity ID
     * @param participantIds Set of participant user IDs
     */
    void cacheActivityParticipants(Long activityId, Set<Long> participantIds);

    /**
     * Gets cached activity participants.
     *
     * @param activityId The activity ID
     * @return Set of participant user IDs
     */
    Set<Long> getActivityParticipants(Long activityId);

    /**
     * Removes activity participants from cache.
     *
     * @param activityId The activity ID
     */
    void removeActivityParticipants(Long activityId);

    // ==================== SESSION MANAGEMENT ====================
    
    /**
     * Maps session ID to user ID.
     *
     * @param sessionId The session ID
     * @param userId The user ID
     */
    void mapSessionToUser(String sessionId, Long userId);

    /**
     * Removes session mapping.
     *
     * @param sessionId The session ID
     */
    void removeSessionMapping(String sessionId);

    /**
     * Gets session ID for a user.
     *
     * @param userId The user ID
     * @return The session ID or null if not found
     */
    String getUserSession(Long userId);

    // ==================== BULK OPERATIONS ====================
    
    /**
     * Cleans up expired data and sessions.
     */
    void cleanupExpiredData();

    /**
     * Gets cache statistics.
     *
     * @return Map containing cache statistics
     */
    Map<String, Object> getCacheStatistics();

    /**
     * Clears all SocketIO cache data (use with caution).
     */
    void clearAllCache();
} 
