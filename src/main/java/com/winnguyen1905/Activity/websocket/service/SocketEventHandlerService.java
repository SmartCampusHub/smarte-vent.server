package com.winnguyen1905.activity.websocket.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.websocket.dto.*;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.common.constant.NotificationType;
import com.winnguyen1905.activity.rest.service.NotificationService;

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
              participantId, "activity_message_received", messageDto);
          if (delivered) deliveredCount++;
        }
      }

      // Send confirmation to sender
      client.sendEvent("activity_message_sent", Map.of(
          "messageId", messageDto.getMessageId(),
          "deliveredTo", deliveredCount,
          "totalParticipants", participantIds.size() - 1,
          "timestamp", Instant.now()));

      log.debug("Activity message sent to {}/{} participants in activity {}",
          deliveredCount, participantIds.size() - 1, messageDto.getActivityId());

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
      if (senderId == null || !senderId.equals(announcementDto.getSenderId())) {
        log.warn("Invalid sender ID in activity announcement from client: {}", client.getSessionId());
        return;
      }

      // Verify user is organizer of the activity
      EActivity activity = activityRepository.findById(announcementDto.getActivityId()).orElse(null);
      if (activity == null) {
        client.sendEvent("announcement_error", Map.of("error", "Activity not found"));
        return;
      }

      boolean isOrganizer = activity.getOrganization().getId().equals(senderId);
      if (!isOrganizer) {
        log.warn("User {} is not an organizer of activity {}", senderId, announcementDto.getActivityId());
        client.sendEvent("announcement_error", Map.of("error", "Not authorized to send announcements"));
        return;
      }

      announcementDto.setTimestamp(Instant.now());
      announcementDto.setMessageType(ActivityChatMessageDto.MessageType.ANNOUNCEMENT);

      // Get all participants of the activity
      Set<Long> participantIds = getCachedActivityParticipants(announcementDto.getActivityId());

      // Send to all participants
      int deliveredCount = 0;
      for (Long participantId : participantIds) {
        boolean delivered = socketIOService.sendNotification(
            participantId, "activity_announcement_received", announcementDto);
        if (delivered) deliveredCount++;
      }

      // Send confirmation to organizer
      client.sendEvent("announcement_sent", Map.of(
          "messageId", announcementDto.getMessageId(),
          "deliveredTo", deliveredCount,
          "totalParticipants", participantIds.size(),
          "timestamp", Instant.now()));

      log.info("Activity announcement sent to {}/{} participants in activity {}",
          deliveredCount, participantIds.size(), announcementDto.getActivityId());

    } catch (Exception e) {
      log.error("Error handling activity announcement: {}", e.getMessage());
      client.sendEvent("announcement_error", Map.of("error", "Failed to send announcement"));
    }
  }

  /**
   * Handles typing start indicator.
   */
  private void handleTypingStart(SocketIOClient client, TypingIndicatorDto typingDto, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      if (userId == null || !userId.equals(typingDto.getUserId())) {
        log.warn("Invalid user ID in typing start from client: {}", client.getSessionId());
        return;
      }

      String sessionId = client.getSessionId().toString();
      
      // Determine conversation type and ID
      boolean isPrivate = typingDto.getReceiverId() != null;
      Long conversationId = isPrivate ? typingDto.getReceiverId() : typingDto.getActivityId();

      if (conversationId == null) {
        log.warn("No conversation ID provided in typing indicator");
        return;
      }

      // Store typing indicator in cache
      socketCacheService.setUserTyping(sessionId, userId, conversationId, isPrivate);

      // Notify other participants
      if (isPrivate) {
        // Private conversation - notify the other user
        socketIOService.sendNotification(conversationId, "user_typing_start", 
            Map.of("userId", userId, "userName", typingDto.getUserName()));
      } else {
        // Activity conversation - notify all participants except the typer
        Set<Long> participantIds = getCachedActivityParticipants(conversationId);
        for (Long participantId : participantIds) {
          if (!participantId.equals(userId)) {
            socketIOService.sendNotification(participantId, "user_typing_start",
                Map.of("userId", userId, "userName", typingDto.getUserName(), "activityId", conversationId));
          }
        }
      }

      log.debug("User {} started typing in {} conversation {}",
          userId, isPrivate ? "private" : "activity", conversationId);

    } catch (Exception e) {
      log.error("Error handling typing start: {}", e.getMessage());
    }
  }

  /**
   * Handles typing stop indicator.
   */
  private void handleTypingStop(SocketIOClient client, TypingIndicatorDto typingDto, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      if (userId == null || !userId.equals(typingDto.getUserId())) {
        log.warn("Invalid user ID in typing stop from client: {}", client.getSessionId());
        return;
      }

      String sessionId = client.getSessionId().toString();
      
      // Remove typing indicator from cache (this will also notify other users)
      socketCacheService.removeUserTyping(sessionId);

      // Determine conversation type and ID for notification
      boolean isPrivate = typingDto.getReceiverId() != null;
      Long conversationId = isPrivate ? typingDto.getReceiverId() : typingDto.getActivityId();

      if (conversationId != null) {
        // Notify other participants that user stopped typing
        if (isPrivate) {
          socketIOService.sendNotification(conversationId, "user_typing_stop",
              Map.of("userId", userId));
        } else {
          Set<Long> participantIds = getCachedActivityParticipants(conversationId);
          for (Long participantId : participantIds) {
            if (!participantId.equals(userId)) {
              socketIOService.sendNotification(participantId, "user_typing_stop",
                  Map.of("userId", userId, "activityId", conversationId));
            }
          }
        }
      }

      log.debug("User {} stopped typing in {} conversation {}",
          userId, isPrivate ? "private" : "activity", conversationId);

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
        log.warn("Invalid user ID in status update from client: {}", client.getSessionId());
        return;
      }

      // Update status in cache
      socketCacheService.setUserStatus(userId, statusDto.getStatus());
      
      // Update last seen timestamp
      socketCacheService.updateLastSeen(userId, Instant.now());

      // Broadcast status update to relevant users (contacts, activity participants, etc.)
      broadcastUserStatusUpdate(statusDto);

      log.debug("Updated status for user {} to {}", userId, statusDto.getStatus());

    } catch (Exception e) {
      log.error("Error handling user status update: {}", e.getMessage());
    }
  }

  /**
   * Handles user heartbeat for presence tracking.
   */
  private void handleUserHeartbeat(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      if (userId != null) {
        // Update last seen timestamp
        socketCacheService.updateLastSeen(userId, Instant.now());
        
        // Send heartbeat acknowledgment
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
      Long senderId = Long.valueOf(data.get("senderId").toString());

      // Notify sender that message was read
      socketIOService.sendNotification(senderId, "message_read_confirmation",
          Map.of("messageId", messageId, "readBy", userId, "readAt", Instant.now()));

      log.debug("Message {} marked as read by user {}", messageId, userId);

    } catch (Exception e) {
      log.error("Error handling message read confirmation: {}", e.getMessage());
    }
  }

  /**
   * Handles joining activity chat.
   */
  private void handleJoinActivityChat(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      Long activityId = Long.valueOf(data.get("activityId").toString());

      // Verify user is participant
      boolean isParticipant = participationDetailRepository.existsByActivityIdAndParticipantIdAndStatus(
          activityId, userId, ParticipationStatus.VERIFIED);

      if (!isParticipant) {
        client.sendEvent("join_chat_error", Map.of("error", "Not authorized to join this activity chat"));
        return;
      }

      // Join the activity chat room
      String roomName = "activity_chat_" + activityId;
      client.joinRoom(roomName);

      // Get activity participants and notify them
      Set<Long> participantIds = getCachedActivityParticipants(activityId);
      EAccountCredentials user = accountRepository.findById(userId).orElse(null);

      if (user != null) {
        for (Long participantId : participantIds) {
          if (!participantId.equals(userId)) {
            socketIOService.sendNotification(participantId, "user_joined_activity_chat",
                Map.of("userId", userId, "userName", user.getFullName(), "activityId", activityId));
          }
        }
      }

      client.sendEvent("joined_activity_chat", Map.of(
          "activityId", activityId,
          "roomName", roomName,
          "participantCount", participantIds.size()));

      log.debug("User {} joined activity chat {}", userId, activityId);

    } catch (Exception e) {
      log.error("Error handling join activity chat: {}", e.getMessage());
      client.sendEvent("join_chat_error", Map.of("error", "Failed to join activity chat"));
    }
  }

  /**
   * Handles leaving activity chat.
   */
  private void handleLeaveActivityChat(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      Long activityId = Long.valueOf(data.get("activityId").toString());

      // Leave the activity chat room
      String roomName = "activity_chat_" + activityId;
      client.leaveRoom(roomName);

      // Notify other participants
      Set<Long> participantIds = getCachedActivityParticipants(activityId);
      EAccountCredentials user = accountRepository.findById(userId).orElse(null);

      if (user != null) {
        for (Long participantId : participantIds) {
          if (!participantId.equals(userId)) {
            socketIOService.sendNotification(participantId, "user_left_activity_chat",
                Map.of("userId", userId, "userName", user.getFullName(), "activityId", activityId));
          }
        }
      }

      client.sendEvent("left_activity_chat", Map.of("activityId", activityId));

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

      // Mark notification as acknowledged (could update database here)
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
      Long senderId = Long.valueOf(data.get("senderId").toString());

      // Notify sender that message was delivered
      socketIOService.sendNotification(senderId, "message_delivery_confirmation",
          Map.of("messageId", messageId, "deliveredTo", userId, "deliveredAt", Instant.now()));

      log.debug("Message {} delivery confirmed by user {}", messageId, userId);

    } catch (Exception e) {
      log.error("Error handling message delivery: {}", e.getMessage());
    }
  }

  /**
   * Handles notification settings updates.
   */
  private void handleNotificationSettingsUpdate(SocketIOClient client, Map<String, Object> data, Object ackSender) {
    try {
      Long userId = getUserIdFromClient(client);
      
      // Update notification settings (could update database here)
      client.sendEvent("notification_settings_updated", Map.of(
          "timestamp", Instant.now(),
          "status", "success"));

      log.debug("Notification settings updated for user {}", userId);

    } catch (Exception e) {
      log.error("Error handling notification settings update: {}", e.getMessage());
    }
  }

  /**
   * Broadcasts user status update to relevant users.
   */
  private void broadcastUserStatusUpdate(UserStatusDto statusDto) {
    // This could be enhanced to send status updates to user's contacts,
    // activity participants they're involved with, etc.
    // For now, it's a placeholder for future enhancement
    log.debug("Broadcasting status update for user {}", statusDto.getUserId());
  }

  /**
   * Extracts user ID from Socket.IO client.
   */
  private Long getUserIdFromClient(SocketIOClient client) {
    try {
      String userIdParam = client.getHandshakeData().getSingleUrlParam(USER_ID_PARAM);
      return userIdParam != null ? Long.valueOf(userIdParam) : null;
    } catch (Exception e) {
      log.error("Error extracting user ID from client: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Public method to get user status.
   */
  public UserStatusDto.UserStatus getUserStatus(Long userId) {
    return socketCacheService.getUserStatus(userId);
  }

  /**
   * Public method to get user last seen.
   */
  public Instant getUserLastSeen(Long userId) {
    return socketCacheService.getLastSeen(userId);
  }

  /**
   * Gets activity participants with caching.
   */
  private Set<Long> getCachedActivityParticipants(Long activityId) {
    Set<Long> cachedParticipants = socketCacheService.getActivityParticipants(activityId);
    
    if (cachedParticipants == null || cachedParticipants.isEmpty()) {
      // Load from database and cache
      List<EParticipationDetail> participants = participationDetailRepository
          .findByActivityIdAndStatus(activityId, ParticipationStatus.VERIFIED);
      
      cachedParticipants = participants.stream()
          .map(p -> p.getParticipant().getId())
          .collect(Collectors.toSet());
      
      if (!cachedParticipants.isEmpty()) {
        socketCacheService.cacheActivityParticipants(activityId, cachedParticipants);
      }
    }
    
    return cachedParticipants;
  }
} 
