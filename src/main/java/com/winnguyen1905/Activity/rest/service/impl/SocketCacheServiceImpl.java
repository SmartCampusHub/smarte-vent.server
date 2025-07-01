package com.winnguyen1905.activity.rest.service.impl;

import com.winnguyen1905.activity.model.dto.UserStatusDto;
import com.winnguyen1905.activity.rest.service.SocketCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis-based implementation of SocketIO caching service.
 * Provides high-performance caching for real-time SocketIO operations with
 * automatic expiration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocketCacheServiceImpl implements SocketCacheService {

  @Qualifier("socketRedisTemplate")
  private final RedisTemplate<String, Object> redisTemplate;

  // Cache key prefixes for different data types
  private static final String USER_STATUS_PREFIX = "socket:user:status:";
  private static final String LAST_SEEN_PREFIX = "socket:user:lastseen:";
  private static final String TYPING_PREFIX = "socket:typing:";
  private static final String ONLINE_USERS_KEY = "socket:online:users";
  private static final String SESSION_USER_PREFIX = "socket:session:user:";
  private static final String USER_SESSION_PREFIX = "socket:user:session:";
  private static final String ACTIVITY_PARTICIPANTS_PREFIX = "socket:activity:participants:";
  private static final String TYPING_CONVERSATION_PREFIX = "socket:typing:conversation:";

  // Cache expiration times
  private static final Duration USER_STATUS_EXPIRY = Duration.ofHours(24);
  private static final Duration LAST_SEEN_EXPIRY = Duration.ofDays(7);
  private static final Duration TYPING_EXPIRY = Duration.ofSeconds(30);
  private static final Duration SESSION_EXPIRY = Duration.ofHours(12);
  private static final Duration ACTIVITY_PARTICIPANTS_EXPIRY = Duration.ofHours(6);

  // ==================== USER STATUS MANAGEMENT ====================

  @Override
  public void setUserStatus(Long userId, UserStatusDto.UserStatus status) {
    try {
      String key = USER_STATUS_PREFIX + userId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
      valueOps.set(key, status.name(), USER_STATUS_EXPIRY);
      log.debug("Set user {} status to {}", userId, status);
    } catch (Exception e) {
      log.error("Failed to set user status for user {}: {}", userId, e.getMessage());
    }
  }

  @Override
  public UserStatusDto.UserStatus getUserStatus(Long userId) {
    try {
      String key = USER_STATUS_PREFIX + userId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
      Object status = valueOps.get(key);

      if (status != null) {
        return UserStatusDto.UserStatus.valueOf(status.toString());
      }
    } catch (Exception e) {
      log.error("Failed to get user status for user {}: {}", userId, e.getMessage());
    }
    return UserStatusDto.UserStatus.OFFLINE;
  }

  @Override
  public void removeUserStatus(Long userId) {
    try {
      String key = USER_STATUS_PREFIX + userId;
      redisTemplate.delete(key);
      log.debug("Removed user status for user {}", userId);
    } catch (Exception e) {
      log.error("Failed to remove user status for user {}: {}", userId, e.getMessage());
    }
  }

  // ==================== LAST SEEN TRACKING ====================

  @Override
  public void updateLastSeen(Long userId, Instant lastSeen) {
    try {
      String key = LAST_SEEN_PREFIX + userId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
      valueOps.set(key, lastSeen.toEpochMilli(), LAST_SEEN_EXPIRY);
      log.debug("Updated last seen for user {} to {}", userId, lastSeen);
    } catch (Exception e) {
      log.error("Failed to update last seen for user {}: {}", userId, e.getMessage());
    }
  }

  @Override
  public Instant getLastSeen(Long userId) {
    try {
      String key = LAST_SEEN_PREFIX + userId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
      Object timestamp = valueOps.get(key);

      if (timestamp != null) {
        return Instant.ofEpochMilli(Long.parseLong(timestamp.toString()));
      }
    } catch (Exception e) {
      log.error("Failed to get last seen for user {}: {}", userId, e.getMessage());
    }
    return null;
  }

  @Override
  public void removeLastSeen(Long userId) {
    try {
      String key = LAST_SEEN_PREFIX + userId;
      redisTemplate.delete(key);
      log.debug("Removed last seen for user {}", userId);
    } catch (Exception e) {
      log.error("Failed to remove last seen for user {}: {}", userId, e.getMessage());
    }
  }

  // ==================== TYPING INDICATORS ====================

  @Override
  public void setUserTyping(String sessionId, Long userId, Long conversationId, boolean isPrivate) {
    try {
      // Map session to user for cleanup
      String sessionKey = TYPING_PREFIX + sessionId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

      Map<String, Object> typingData = new HashMap<>();
      typingData.put("userId", userId);
      typingData.put("conversationId", conversationId);
      typingData.put("isPrivate", isPrivate);

      valueOps.set(sessionKey, typingData, TYPING_EXPIRY);

      // Add to conversation typing set
      String conversationKey = TYPING_CONVERSATION_PREFIX + (isPrivate ? "private:" : "activity:") + conversationId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      setOps.add(conversationKey, userId);
      redisTemplate.expire(conversationKey, TYPING_EXPIRY);

      log.debug("Set user {} typing in conversation {} (private: {})", userId, conversationId, isPrivate);
    } catch (Exception e) {
      log.error("Failed to set typing indicator for session {}: {}", sessionId, e.getMessage());
    }
  }

  @Override
  public void removeUserTyping(String sessionId) {
    try {
      String sessionKey = TYPING_PREFIX + sessionId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

      @SuppressWarnings("unchecked")
      Map<String, Object> typingData = (Map<String, Object>) valueOps.get(sessionKey);

      if (typingData != null) {
        Long userId = Long.valueOf(typingData.get("userId").toString());
        Long conversationId = Long.valueOf(typingData.get("conversationId").toString());
        boolean isPrivate = Boolean.parseBoolean(typingData.get("isPrivate").toString());

        // Remove from conversation typing set
        String conversationKey = TYPING_CONVERSATION_PREFIX + (isPrivate ? "private:" : "activity:") + conversationId;
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.remove(conversationKey, userId);

        log.debug("Removed user {} from typing in conversation {} (private: {})", userId, conversationId, isPrivate);
      }

      redisTemplate.delete(sessionKey);
    } catch (Exception e) {
      log.error("Failed to remove typing indicator for session {}: {}", sessionId, e.getMessage());
    }
  }

  @Override
  public Set<Long> getTypingUsers(Long conversationId, boolean isPrivate) {
    try {
      String conversationKey = TYPING_CONVERSATION_PREFIX + (isPrivate ? "private:" : "activity:") + conversationId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      Set<Object> typingUserIds = setOps.members(conversationKey);

      if (typingUserIds != null) {
        return typingUserIds.stream()
            .map(id -> Long.valueOf(id.toString()))
            .collect(Collectors.toSet());
      }
    } catch (Exception e) {
      log.error("Failed to get typing users for conversation {}: {}", conversationId, e.getMessage());
    }
    return Set.of();
  }

  @Override
  public Long getUserIdBySession(String sessionId) {
    try {
      String sessionKey = TYPING_PREFIX + sessionId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

      @SuppressWarnings("unchecked")
      Map<String, Object> typingData = (Map<String, Object>) valueOps.get(sessionKey);

      if (typingData != null) {
        return Long.valueOf(typingData.get("userId").toString());
      }
    } catch (Exception e) {
      log.error("Failed to get user ID for session {}: {}", sessionId, e.getMessage());
    }
    return null;
  }

  // ==================== ONLINE USERS TRACKING ====================

  @Override
  public void addOnlineUser(Long userId, String sessionId) {
    try {
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      setOps.add(ONLINE_USERS_KEY, userId);

      // Map session to user
      mapSessionToUser(sessionId, userId);

      log.debug("Added user {} to online users", userId);
    } catch (Exception e) {
      log.error("Failed to add online user {}: {}", userId, e.getMessage());
    }
  }

  @Override
  public void removeOnlineUser(Long userId) {
    try {
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      setOps.remove(ONLINE_USERS_KEY, userId);

      // Remove user session mapping
      String userSessionKey = USER_SESSION_PREFIX + userId;
      String sessionId = (String) redisTemplate.opsForValue().get(userSessionKey);
      if (sessionId != null) {
        removeSessionMapping(sessionId);
        redisTemplate.delete(userSessionKey);
      }

      log.debug("Removed user {} from online users", userId);
    } catch (Exception e) {
      log.error("Failed to remove online user {}: {}", userId, e.getMessage());
    }
  }

  @Override
  public Set<Long> getOnlineUsers() {
    try {
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      Set<Object> onlineUserIds = setOps.members(ONLINE_USERS_KEY);

      if (onlineUserIds != null) {
        return onlineUserIds.stream()
            .map(id -> Long.valueOf(id.toString()))
            .collect(Collectors.toSet());
      }
    } catch (Exception e) {
      log.error("Failed to get online users: {}", e.getMessage());
    }
    return Set.of();
  }

  @Override
  public boolean isUserOnline(Long userId) {
    try {
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      return Boolean.TRUE.equals(setOps.isMember(ONLINE_USERS_KEY, userId));
    } catch (Exception e) {
      log.error("Failed to check if user {} is online: {}", userId, e.getMessage());
      return false;
    }
  }

  // ==================== ACTIVITY PARTICIPANTS CACHE ====================

  @Override
  public void cacheActivityParticipants(Long activityId, Set<Long> participantIds) {
    try {
      String key = ACTIVITY_PARTICIPANTS_PREFIX + activityId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();

      // Clear existing participants
      redisTemplate.delete(key);

      // Add new participants
      if (!participantIds.isEmpty()) {
        setOps.add(key, participantIds.toArray());
        redisTemplate.expire(key, ACTIVITY_PARTICIPANTS_EXPIRY);
      }

      log.debug("Cached {} participants for activity {}", participantIds.size(), activityId);
    } catch (Exception e) {
      log.error("Failed to cache participants for activity {}: {}", activityId, e.getMessage());
    }
  }

  @Override
  public Set<Long> getActivityParticipants(Long activityId) {
    try {
      String key = ACTIVITY_PARTICIPANTS_PREFIX + activityId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      Set<Object> participantIds = setOps.members(key);

      if (participantIds != null) {
        return participantIds.stream()
            .map(id -> Long.valueOf(id.toString()))
            .collect(Collectors.toSet());
      }
    } catch (Exception e) {
      log.error("Failed to get participants for activity {}: {}", activityId, e.getMessage());
    }
    return Set.of();
  }

  @Override
  public void removeActivityParticipants(Long activityId) {
    try {
      String key = ACTIVITY_PARTICIPANTS_PREFIX + activityId;
      redisTemplate.delete(key);
      log.debug("Removed participants cache for activity {}", activityId);
    } catch (Exception e) {
      log.error("Failed to remove participants for activity {}: {}", activityId, e.getMessage());
    }
  }

  // ==================== SESSION MANAGEMENT ====================

  @Override
  public void mapSessionToUser(String sessionId, Long userId) {
    try {
      String sessionUserKey = SESSION_USER_PREFIX + sessionId;
      String userSessionKey = USER_SESSION_PREFIX + userId;

      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
      valueOps.set(sessionUserKey, userId, SESSION_EXPIRY);
      valueOps.set(userSessionKey, sessionId, SESSION_EXPIRY);

      log.debug("Mapped session {} to user {}", sessionId, userId);
    } catch (Exception e) {
      log.error("Failed to map session {} to user {}: {}", sessionId, userId, e.getMessage());
    }
  }

  @Override
  public void removeSessionMapping(String sessionId) {
    try {
      String sessionUserKey = SESSION_USER_PREFIX + sessionId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

      Object userId = valueOps.get(sessionUserKey);
      if (userId != null) {
        String userSessionKey = USER_SESSION_PREFIX + userId;
        redisTemplate.delete(userSessionKey);
      }

      redisTemplate.delete(sessionUserKey);
      log.debug("Removed session mapping for session {}", sessionId);
    } catch (Exception e) {
      log.error("Failed to remove session mapping for session {}: {}", sessionId, e.getMessage());
    }
  }

  @Override
  public String getUserSession(Long userId) {
    try {
      String userSessionKey = USER_SESSION_PREFIX + userId;
      ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
      Object sessionId = valueOps.get(userSessionKey);

      return sessionId != null ? sessionId.toString() : null;
    } catch (Exception e) {
      log.error("Failed to get session for user {}: {}", userId, e.getMessage());
      return null;
    }
  }

  // ==================== BULK OPERATIONS ====================

  @Override
  public void cleanupExpiredData() {
    try {
      log.info("Starting SocketIO cache cleanup...");

      // Redis automatically handles TTL expiration, but we can clean up orphaned data
      // Remove sessions without corresponding online users
      Set<Long> onlineUsers = getOnlineUsers();
      Set<String> keysToDelete = redisTemplate.keys(SESSION_USER_PREFIX + "*");

      if (keysToDelete != null) {
        for (String key : keysToDelete) {
          Object userId = redisTemplate.opsForValue().get(key);
          if (userId != null && !onlineUsers.contains(Long.valueOf(userId.toString()))) {
            String sessionId = key.substring(SESSION_USER_PREFIX.length());
            removeSessionMapping(sessionId);
          }
        }
      }

      log.info("SocketIO cache cleanup completed");
    } catch (Exception e) {
      log.error("Failed to cleanup expired data: {}", e.getMessage());
    }
  }

  @Override
  public Map<String, Object> getCacheStatistics() {
    Map<String, Object> stats = new HashMap<>();

    try {
      stats.put("onlineUsersCount", getOnlineUsers().size());
      stats.put("userStatusKeysCount", getKeyCount(USER_STATUS_PREFIX + "*"));
      stats.put("lastSeenKeysCount", getKeyCount(LAST_SEEN_PREFIX + "*"));
      stats.put("typingKeysCount", getKeyCount(TYPING_PREFIX + "*"));
      stats.put("sessionKeysCount", getKeyCount(SESSION_USER_PREFIX + "*"));
      stats.put("activityParticipantsKeysCount", getKeyCount(ACTIVITY_PARTICIPANTS_PREFIX + "*"));

      log.debug("Generated cache statistics: {}", stats);
    } catch (Exception e) {
      log.error("Failed to get cache statistics: {}", e.getMessage());
      stats.put("error", "Failed to retrieve statistics");
    }

    return stats;
  }

  @Override
  public void clearAllCache() {
    try {
      log.warn("Clearing all SocketIO cache data...");

      String[] prefixes = {
          USER_STATUS_PREFIX + "*",
          LAST_SEEN_PREFIX + "*",
          TYPING_PREFIX + "*",
          SESSION_USER_PREFIX + "*",
          USER_SESSION_PREFIX + "*",
          ACTIVITY_PARTICIPANTS_PREFIX + "*",
          TYPING_CONVERSATION_PREFIX + "*"
      };

      for (String pattern : prefixes) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
          redisTemplate.delete(keys);
        }
      }

      redisTemplate.delete(ONLINE_USERS_KEY);

      log.warn("All SocketIO cache data cleared");
    } catch (Exception e) {
      log.error("Failed to clear cache: {}", e.getMessage());
    }
  }

  // ==================== HELPER METHODS ====================

  private long getKeyCount(String pattern) {
    try {
      Set<String> keys = redisTemplate.keys(pattern);
      return keys != null ? keys.size() : 0;
    } catch (Exception e) {
      log.error("Failed to count keys for pattern {}: {}", pattern, e.getMessage());
      return 0;
    }
  }
}
