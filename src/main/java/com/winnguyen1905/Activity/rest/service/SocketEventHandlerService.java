package com.winnguyen1905.activity.rest.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.winnguyen1905.activity.model.dto.*;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.common.constant.NotificationType;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for handling Socket.IO events related to messaging and notifications.
 * Provides comprehensive event handling for real-time communication features.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocketEventHandlerService {

  private final SocketIOServer socketIOServer;
  private final SocketIOService socketIOService;
  private final SocketCacheService socketCacheService;
  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final ParticipationDetailRepository participationDetailRepository;
  private final NotificationService notificationService;

  // Event constants
  private static final String USER_ID_PARAM = "userId";

  /**
   * Initializes all socket event handlers.
   */
  @PostConstruct
  private void initializeEventHandlers() {
    log.info("Initializing Socket.IO event handlers...");

    setupMessageEventHandlers();
    setupNotificationEventHandlers();
    setupUserStatusEventHandlers();
    setupActivityEventHandlers();
    setupTypingEventHandlers();

    log.info("Socket.IO event handlers initialized successfully");
  }

  /**
   * Sets up message-related event handlers.
   */
  private void setupMessageEventHandlers() {
    // Handle private messages
    socketIOServer.addEventListener("send_private_message", ChatMessageDto.class,
        this::handlePrivateMessage);

    // Handle message read confirmations
    socketIOServer.addEventListener("mark_message_read", Map.class,
        this::handleMessageReadConfirmation);

    // Handle message delivery confirmations
    socketIOServer.addEventListener("message_delivered", Map.class,
        this::handleMessageDelivered);
  }

  /**
   * Sets up notification-related event handlers.
   */
  private void setupNotificationEventHandlers() {
    // Handle notification acknowledgments
    socketIOServer.addEventListener("notification_acknowledged", Map.class,
        this::handleNotificationAcknowledgment);

    // Handle notification settings updates
    socketIOServer.addEventListener("update_notification_settings", Map.class,
        this::handleNotificationSettingsUpdate);
  }

  /**
   * Sets up user status event handlers.
   */
  private void setupUserStatusEventHandlers() {
    // Handle user status updates
    socketIOServer.addEventListener("update_user_status", UserStatusDto.class,
        this::handleUserStatusUpdate);

    // Handle user presence heartbeat
    socketIOServer.addEventListener("user_heartbeat", Map.class,
        this::handleUserHeartbeat);
  }

  /**
   * Sets up activity-related event handlers.
   */
  private void setupActivityEventHandlers() {
    // Handle activity chat messages
    socketIOServer.addEventListener("send_activity_message", ActivityChatMessageDto.class,
        this::handleActivityMessage);

    // Handle joining activity chat
    socketIOServer.addEventListener("join_activity_chat", Map.class,
        this::handleJoinActivityChat);

    // Handle leaving activity chat
    socketIOServer.addEventListener("leave_activity_chat", Map.class,
        this::handleLeaveActivityChat);

    // Handle activity announcements
    socketIOServer.addEventListener("send_activity_announcement", ActivityChatMessageDto.class,
        this::handleActivityAnnouncement);
  }

  /**
   * Sets up typing indicator event handlers.
   */
  private void setupTypingEventHandlers() {
    // Handle typing start
    socketIOServer.addEventListener("typing_start", TypingIndicatorDto.class,
        this::handleTypingStart);

    // Handle typing stop
    socketIOServer.addEventListener("typing_stop", TypingIndicatorDto.class,
        this::handleTypingStop);
  }

  /**
   * Handles private message sending between users.
   */
  private void handlePrivateMessage(SocketIOClient client, ChatMessageDto messageDto, Object ackSender) {
    try {
      Long senderId = getUserIdFromClient(client);
      if (senderId == null || !senderId.equals(messageDto.getSenderId())) {
        log.warn("Invalid sender ID in private message from client: {}", client.getSessionId());
        return;
      }

      // Set timestamp and mark as unread
      messageDto.setTimestamp(Instant.now());
      messageDto.setIsRead(false);

      // Send to receiver if online
      boolean delivered = socketIOService.sendNotification(
          messageDto.getReceiverId(),
          "private_message_received",
          messageDto);

      // Send delivery confirmation to sender
      client.sendEvent("message_delivery_status", Map.of(
          "messageId", messageDto.getMessageId(),
          "delivered", delivered,
          "timestamp", Instant.now()));

      log.debug("Private message sent from user {} to user {}", senderId, messageDto.getReceiverId());

    } catch (Exception e) {
      log.error("Error handling private message: {}", e.getMessage());
      client.sendEvent("message_error", Map.of("error", "Failed to send message"));
    }
  }

  /**
   * Handles activity chat messages.
   */
  private void handleActivityMessage(SocketIOClient client, ActivityChatMessageDto messageDto, Object ackSender) {
    try {
      Long senderId = getUserIdFromClient(client);
      if (senderId == null || !senderId.equals(messageDto.getSenderId())) {
        log.warn("Invalid sender ID in activity message from client: {}", client.getSessionId());
        return;
      }

      // Verify user is participant of the activity
      boolean isParticipant = participationDetailRepository.existsByActivityIdAndParticipantIdAndStatus(
          messageDto.getActivityId(), senderId, ParticipationStatus.VERIFIED);

      if (!isParticipant) {
        log.warn("User {} is not a participant of activity {}", senderId, messageDto.getActivityId());
        client.sendEvent("message_error", Map.of("error", "Not authorized to send messages in this activity"));
        return;
      }

      messageDto.setTimestamp(Instant.now());

      // Get all participants of the activity (with Redis caching)
      Set<Long> participantIds = getCachedActivityParticipants(messageDto.getActivityId());
      
      // Send to all participants except sender
      int deliveredCount = 0;
      for (Long participantId : participantIds) {
        if (!participantId.equals(senderId)) {
          boolean delivered = socketIOService.sendNotification(
              participantId,
              "activity_message_received",
              messageDto);
          if (delivered)
            deliveredCount++;
        }
      }

      // Send confirmation to sender
      client.sendEvent("activity_message_delivery_status", Map.of(
          "messageId", messageDto.getMessageId(),
          "activityId", messageDto.getActivityId(),
          "deliveredToCount", deliveredCount,
          "totalParticipants", participantIds.size() - 1,
          "timestamp", Instant.now()));

      log.debug("Activity message sent in activity {} by user {} to {} participants",
          messageDto.getActivityId(), senderId, deliveredCount);

    } catch (Exception e) {
      log.error("Error handling activity message: {}", e.getMessage());
      client.sendEvent("message_error", Map.of("error", "Failed to send activity message"));
    }
  }

  /**
   * Handles activity announcements (organizer only).
   */
  private void handleActivityAnnouncement(SocketIOClient client, ActivityChatMessageDto announcementDto,
      Object ackSender) {
    try {
      Long senderId = getUserIdFromClient(client);
      if (senderId == null) {
        return;
      }

      // Verify user is organizer of the activity
      EActivity activity = activityRepository.findById(announcementDto.getActivityId()).orElse(null);
      if (activity == null || !activity.getOrganization().getId().equals(senderId)) {
        log.warn("User {} is not authorized to send announcements for activity {}",
            senderId, announcementDto.getActivityId());
        client.sendEvent("message_error", Map.of("error", "Not authorized to send announcements"));
        return;
      }

      announcementDto.setTimestamp(Instant.now());
      announcementDto.setMessageType(ActivityChatMessageDto.MessageType.ANNOUNCEMENT);

      // Get all participants (with Redis caching)
      Set<Long> participantIds = getCachedActivityParticipants(announcementDto.getActivityId());

      // Send to all participants
      int deliveredCount = 0;
      for (Long participantId : participantIds) {
        boolean delivered = socketIOService.sendNotification(
            participantId,
            "activity_announcement_received",
            announcementDto);
        if (delivered)
          deliveredCount++;
      }

      // Also send as regular notification
      for (Long participantId : participantIds) {
        NotificationDto notification = NotificationDto.builder()
            .title("New Announcement: " + activity.getActivityName())
            .content(announcementDto.getContent())
            .notificationType(NotificationType.ACTIVITY)
            .receiverId(participantId)
            .build();

        try {
          notificationService.sendNotification(null, notification);
        } catch (Exception e) {
          log.error("Failed to send announcement notification: {}", e.getMessage());
        }
      }

      client.sendEvent("announcement_delivery_status", Map.of(
          "messageId", announcementDto.getMessageId(),
          "activityId", announcementDto.getActivityId(),
          "deliveredToCount", deliveredCount,
          "totalParticipants", participantIds.size(),
          "timestamp", Instant.now()));

      log.info("Activity announcement sent in activity {} by organizer {} to {} participants",
          announcementDto.getActivityId(), senderId, deliveredCount);

    } catch (Exception e) {
      log.error("Error handling activity announcement: {}", e.getMessage());
      client.sendEvent("message_error", Map.of("error", "Failed to send announcement"));
    }
  }

  /**
   * Handles typing indicators.
   */
  private void handleTypingStart(SocketIOClient client, TypingIndicatorDto typingDto, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      if (userId == null || !userId.equals(typingDto.getUserId())) {
        return;
      }

      typingDto.setIsTyping(true);
      socketCacheService.setUserTyping(client.getSessionId().toString(), userId, 
          typingDto.getActivityId() != null ? typingDto.getActivityId() : typingDto.getReceiverId(), 
          typingDto.getActivityId() == null);

      if (typingDto.getActivityId() != null) {
        // Activity chat typing (with Redis caching)
        Set<Long> participantIds = getCachedActivityParticipants(typingDto.getActivityId());

        for (Long participantId : participantIds) {
          if (!participantId.equals(userId)) {
            socketIOService.sendNotification(
                participantId,
                "user_typing_in_activity",
                typingDto);
          }
        }
      } else if (typingDto.getReceiverId() != null) {
        // Private chat typing
        socketIOService.sendNotification(
            typingDto.getReceiverId(),
            "user_typing_private",
            typingDto);
      }

    } catch (Exception e) {
      log.error("Error handling typing start: {}", e.getMessage());
    }
  }

  /**
   * Handles stopping typing indicators.
   */
  private void handleTypingStop(SocketIOClient client, TypingIndicatorDto typingDto, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      if (userId == null) {
        return;
      }

      typingDto.setIsTyping(false);
      socketCacheService.removeUserTyping(client.getSessionId().toString());

      if (typingDto.getActivityId() != null) {
        // Activity chat typing (with Redis caching)
        Set<Long> participantIds = getCachedActivityParticipants(typingDto.getActivityId());

        for (Long participantId : participantIds) {
          if (!participantId.equals(userId)) {
            socketIOService.sendNotification(
                participantId,
                "user_stopped_typing_in_activity",
                typingDto);
          }
        }
      } else if (typingDto.getReceiverId() != null) {
        // Private chat typing
        socketIOService.sendNotification(
            typingDto.getReceiverId(),
            "user_stopped_typing_private",
            typingDto);
      }

    } catch (Exception e) {
      log.error("Error handling typing stop: {}", e.getMessage());
    }
  }

  /**
   * Handles user status updates.
   */
  private void handleUserStatusUpdate(SocketIOClient client, UserStatusDto statusDto, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      if (userId == null || !userId.equals(statusDto.getUserId())) {
        return;
      }

      socketCacheService.setUserStatus(userId, statusDto.getStatus());
      socketCacheService.updateLastSeen(userId, Instant.now());

      // Broadcast status update to relevant users (friends, activity participants,
      // etc.)
      broadcastUserStatusUpdate(statusDto);

      log.debug("User {} status updated to {}", userId, statusDto.getStatus());

    } catch (Exception e) {
      log.error("Error handling user status update: {}", e.getMessage());
    }
  }

  /**
   * Handles user heartbeat for presence detection.
   */
  private void handleUserHeartbeat(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      if (userId != null) {
        socketCacheService.updateLastSeen(userId, Instant.now());

        // Send heartbeat response
        client.sendEvent("heartbeat_ack", Map.of(
            "timestamp", Instant.now(),
            "status", "online"));
      }
    } catch (Exception e) {
      log.error("Error handling user heartbeat: {}", e.getMessage());
    }
  }

  /**
   * Handles message read confirmations.
   */
  private void handleMessageReadConfirmation(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      Long messageId = Long.valueOf(data.get("messageId").toString());
      Long senderId = data.get("senderId") != null ? Long.valueOf(data.get("senderId").toString()) : null;

      if (senderId != null) {
        // Notify sender that message was read
        socketIOService.sendNotification(senderId, "message_read_confirmation", Map.of(
            "messageId", messageId,
            "readBy", userId,
            "readAt", Instant.now()));
      }

      log.debug("Message {} marked as read by user {}", messageId, userId);

    } catch (Exception e) {
      log.error("Error handling message read confirmation: {}", e.getMessage());
    }
  }

  /**
   * Handles joining activity chat rooms.
   */
  private void handleJoinActivityChat(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      Long activityId = Long.valueOf(data.get("activityId").toString());

      // Verify user is participant
      boolean isParticipant = participationDetailRepository.existsByActivityIdAndParticipantIdAndStatus(
          activityId, userId, ParticipationStatus.VERIFIED);

      if (isParticipant) {
        String roomName = "activity_chat_" + activityId;
        client.joinRoom(roomName);

        // Notify others in the room
        client.getNamespace().getRoomOperations(roomName).sendEvent("user_joined_chat", Map.of(
            "userId", userId,
            "activityId", activityId,
            "timestamp", Instant.now()));

        client.sendEvent("joined_activity_chat", Map.of(
            "activityId", activityId,
            "status", "success"));

        log.debug("User {} joined activity chat {}", userId, activityId);
      } else {
        client.sendEvent("joined_activity_chat", Map.of(
            "activityId", activityId,
            "status", "error",
            "message", "Not authorized to join this activity chat"));
      }

    } catch (Exception e) {
      log.error("Error handling join activity chat: {}", e.getMessage());
    }
  }

  /**
   * Handles leaving activity chat rooms.
   */
  private void handleLeaveActivityChat(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      Long activityId = Long.valueOf(data.get("activityId").toString());

      String roomName = "activity_chat_" + activityId;
      client.leaveRoom(roomName);

      // Notify others in the room
      client.getNamespace().getRoomOperations(roomName).sendEvent("user_left_chat", Map.of(
          "userId", userId,
          "activityId", activityId,
          "timestamp", Instant.now()));

      client.sendEvent("left_activity_chat", Map.of(
          "activityId", activityId,
          "status", "success"));

      log.debug("User {} left activity chat {}", userId, activityId);

    } catch (Exception e) {
      log.error("Error handling leave activity chat: {}", e.getMessage());
    }
  }

  /**
   * Handles notification acknowledgments.
   */
  private void handleNotificationAcknowledgment(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      Long notificationId = Long.valueOf(data.get("notificationId").toString());

      // You could update notification as acknowledged in database here
      log.debug("Notification {} acknowledged by user {}", notificationId, userId);

    } catch (Exception e) {
      log.error("Error handling notification acknowledgment: {}", e.getMessage());
    }
  }

  /**
   * Handles message delivery confirmations.
   */
  private void handleMessageDelivered(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      Long messageId = Long.valueOf(data.get("messageId").toString());
      Long senderId = data.get("senderId") != null ? Long.valueOf(data.get("senderId").toString()) : null;

      if (senderId != null) {
        socketIOService.sendNotification(senderId, "message_delivery_confirmation", Map.of(
            "messageId", messageId,
            "deliveredTo", userId,
            "deliveredAt", Instant.now()));
      }

    } catch (Exception e) {
      log.error("Error handling message delivery confirmation: {}", e.getMessage());
    }
  }

  /**
   * Handles notification settings updates.
   */
  private void handleNotificationSettingsUpdate(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      // Process notification settings update
      // You could save these settings to database

      client.sendEvent("notification_settings_updated", Map.of(
          "status", "success",
          "timestamp", Instant.now()));

      log.debug("Notification settings updated for user {}", userId);

    } catch (Exception e) {
      log.error("Error handling notification settings update: {}", e.getMessage());
    }
  }

  /**
   * Broadcasts user status updates to relevant users.
   */
  private void broadcastUserStatusUpdate(UserStatusDto statusDto) {
    // You could implement logic here to determine which users should receive this
    // status update
    // For example, participants in common activities, friends, etc.

    // For now, broadcast to all connected users (you might want to optimize this)
    socketIOService.broadcastNotification("user_status_updated", statusDto);
  }

  /**
   * Extracts user ID from client session.
   */
  private Long getUserIdFromClient(SocketIOClient client) {
    try {
      String userIdParam = client.getHandshakeData().getSingleUrlParam(USER_ID_PARAM);
      return userIdParam != null ? Long.parseLong(userIdParam) : null;
    } catch (NumberFormatException e) {
      log.warn("Invalid user ID format from client: {}", client.getSessionId());
      return null;
    }
  }

  /**
   * Gets current user status.
   */
  public UserStatusDto.UserStatus getUserStatus(Long userId) {
    return socketCacheService.getUserStatus(userId);
  }

  /**
   * Gets user's last seen timestamp.
   */
  public Instant getUserLastSeen(Long userId) {
    return socketCacheService.getLastSeen(userId);
  }

  // ==================== HELPER METHODS ====================

  /**
   * Gets cached activity participants with fallback to database.
   *
   * @param activityId The activity ID
   * @return Set of participant user IDs
   */
  private Set<Long> getCachedActivityParticipants(Long activityId) {
    try {
      // Try to get from cache first
      Set<Long> cachedParticipants = socketCacheService.getActivityParticipants(activityId);
      
      if (!cachedParticipants.isEmpty()) {
        log.debug("Retrieved {} participants from cache for activity {}", cachedParticipants.size(), activityId);
        return cachedParticipants;
      }
      
      // Cache miss - fetch from database
      List<EParticipationDetail> participants = participationDetailRepository
          .findByActivityIdAndStatus(activityId, ParticipationStatus.VERIFIED);
      
      Set<Long> participantIds = participants.stream()
          .map(p -> p.getParticipant().getId())
          .collect(Collectors.toSet());
      
      // Cache the result for future use
      if (!participantIds.isEmpty()) {
        socketCacheService.cacheActivityParticipants(activityId, participantIds);
        log.debug("Cached {} participants for activity {} from database", participantIds.size(), activityId);
      }
      
      return participantIds;
    } catch (Exception e) {
      log.error("Failed to get activity participants for activity {}: {}", activityId, e.getMessage());
      
      // Fallback to direct database query
      List<EParticipationDetail> participants = participationDetailRepository
          .findByActivityIdAndStatus(activityId, ParticipationStatus.VERIFIED);
      
      return participants.stream()
          .map(p -> p.getParticipant().getId())
          .collect(Collectors.toSet());
    }
  }
}
