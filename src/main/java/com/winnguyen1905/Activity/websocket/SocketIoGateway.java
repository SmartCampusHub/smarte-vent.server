package com.winnguyen1905.activity.websocket;

import com.corundumstudio.socketio.SocketIOServer;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.common.constant.NotificationType;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.websocket.dto.*;
import com.winnguyen1905.activity.websocket.service.SocketCacheService;
import com.winnguyen1905.activity.websocket.service.SocketEventHandlerService;
import com.winnguyen1905.activity.websocket.service.SocketIOService;
import com.winnguyen1905.activity.auth.JwtService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Main Socket.IO Gateway for Activity Management System.
 * Provides comprehensive real-time communication features for activity management,
 * including activity status updates, messaging, notifications, and participant management.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SocketIoGateway {

    private final SocketIOServer socketIOServer;
    private final SocketIOService socketIOService;
    private final SocketEventHandlerService socketEventHandlerService;
    private final SocketCacheService socketCacheService;
    private final ActivityRepository activityRepository;
    private final ParticipationDetailRepository participationDetailRepository;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;

    // Event type constants
    private static final String ACTIVITY_STATUS_CHANGED = "activity_status_changed";
    private static final String ACTIVITY_UPDATED = "activity_updated";
    private static final String ACTIVITY_PARTICIPANT_JOINED = "activity_participant_joined";
    private static final String ACTIVITY_PARTICIPANT_LEFT = "activity_participant_left";
    private static final String ACTIVITY_MESSAGE_BROADCAST = "activity_message_broadcast";
    private static final String ACTIVITY_ANNOUNCEMENT = "activity_announcement";
    private static final String ACTIVITY_REMINDER = "activity_reminder";
    private static final String ACTIVITY_EMERGENCY_ALERT = "activity_emergency_alert";

    // Room naming conventions
    private static final String ACTIVITY_ROOM_PREFIX = "activity_";
    private static final String ORGANIZER_ROOM_PREFIX = "organizer_";
    private static final String PARTICIPANT_ROOM_PREFIX = "participant_";

    @PostConstruct
    private void initializeGateway() {
        log.info("Initializing SocketIO Gateway for Activity Management...");
        setupActivityEventHandlers();
        log.info("SocketIO Gateway initialized successfully");
    }

    /**
     * Sets up activity-specific event handlers
     */
    private void setupActivityEventHandlers() {
        // Activity management events
        socketIOServer.addEventListener("join_activity_room", Map.class, this::handleJoinActivityRoom);
        socketIOServer.addEventListener("leave_activity_room", Map.class, this::handleLeaveActivityRoom);
        socketIOServer.addEventListener("get_activity_participants", Map.class, this::handleGetActivityParticipants);
        
        // Activity messaging events
        socketIOServer.addEventListener("broadcast_to_activity", ActivityChatMessageDto.class, this::handleActivityBroadcast);
        socketIOServer.addEventListener("send_emergency_alert", Map.class, this::handleEmergencyAlert);
        
        // Activity status monitoring
        socketIOServer.addEventListener("subscribe_activity_updates", Map.class, this::handleSubscribeActivityUpdates);
        socketIOServer.addEventListener("unsubscribe_activity_updates", Map.class, this::handleUnsubscribeActivityUpdates);
    }

    // ===========================================
    // ACTIVITY STATUS CHANGE NOTIFICATIONS
    // ===========================================

    /**
     * Broadcasts activity status change to all participants
     */
    public void broadcastActivityStatusChange(EActivity activity, ActivityStatus oldStatus, ActivityStatus newStatus) {
        log.info("Broadcasting activity status change for activity {}: {} -> {}", 
                 activity.getId(), oldStatus, newStatus);

        SocketNotificationDto notification = SocketNotificationDto.builder()
                .title("Activity Status Updated")
                .message(String.format("Activity '%s' status changed from %s to %s", 
                        activity.getActivityName(), oldStatus, newStatus))
                .type(NotificationType.ACTIVITY)
                .activityId(activity.getId())
                .timestamp(Instant.now())
                .activityName(activity.getActivityName())
                .activityStartDate(activity.getStartDate())
                .build();

        // Send to all activity participants
        sendToActivityParticipants(activity.getId(), ACTIVITY_STATUS_CHANGED, notification);
        
        // Send to activity organizers
        sendToActivityOrganizers(activity.getId(), ACTIVITY_STATUS_CHANGED, notification);
    }

    /**
     * Broadcasts activity information updates (time, location, details)
     */
    public void broadcastActivityUpdate(EActivity activity, String updateType, String updateMessage) {
        log.info("Broadcasting activity update for activity {}: {}", activity.getId(), updateType);

        ActivityUpdateDto updateDto = ActivityUpdateDto.builder()
                .activityId(activity.getId())
                .activityName(activity.getActivityName())
                .updateType(updateType)
                .updateMessage(updateMessage)
                .timestamp(Instant.now())
                .startDate(activity.getStartDate())
                .endDate(activity.getEndDate())
                .venue(activity.getVenue())
                .build();

        sendToActivityParticipants(activity.getId(), ACTIVITY_UPDATED, updateDto);
    }

    /**
     * Sends activity reminders based on time proximity
     */
    public void sendActivityReminder(EActivity activity, long daysUntilStart) {
        log.info("Sending activity reminder for activity {}: {} days until start", 
                 activity.getId(), daysUntilStart);

        String title = getActivityReminderTitle(daysUntilStart);
        String message = getActivityReminderMessage(activity, daysUntilStart);

        SocketNotificationDto reminder = SocketNotificationDto.builder()
                .title(title)
                .message(message)
                .type(NotificationType.ACTIVITY)
                .activityId(activity.getId())
                .timestamp(Instant.now())
                .daysUntilStart(daysUntilStart)
                .activityName(activity.getActivityName())
                .activityStartDate(activity.getStartDate())
                .build();

        sendToActivityParticipants(activity.getId(), ACTIVITY_REMINDER, reminder);
    }

    // ===========================================
    // PARTICIPANT MANAGEMENT
    // ===========================================

    /**
     * Notifies when a new participant joins an activity
     */
    public void broadcastParticipantJoined(EActivity activity, EAccountCredentials participant) {
        log.info("Broadcasting participant joined for activity {}: user {}", 
                 activity.getId(), participant.getId());

        ParticipantUpdateDto participantUpdate = ParticipantUpdateDto.builder()
                .activityId(activity.getId())
                .activityName(activity.getActivityName())
                .participantId(participant.getId())
                .participantName(participant.getFullName())
                .participantEmail(participant.getEmail())
                .updateType("JOINED")
                .timestamp(Instant.now())
                .currentParticipantCount(activity.getCurrentParticipants())
                .maxParticipants(activity.getCapacityLimit())
                .build();

        // Send to existing participants (excluding the new participant)
        sendToActivityParticipantsExcept(activity.getId(), participant.getId(), 
                                        ACTIVITY_PARTICIPANT_JOINED, participantUpdate);
        
        // Send welcome message to new participant
        sendActivityWelcomeMessage(activity, participant);
    }

    /**
     * Notifies when a participant leaves an activity
     */
    public void broadcastParticipantLeft(EActivity activity, EAccountCredentials participant) {
        log.info("Broadcasting participant left for activity {}: user {}", 
                 activity.getId(), participant.getId());

        ParticipantUpdateDto participantUpdate = ParticipantUpdateDto.builder()
                .activityId(activity.getId())
                .activityName(activity.getActivityName())
                .participantId(participant.getId())
                .participantName(participant.getFullName())
                .participantEmail(participant.getEmail())
                .updateType("LEFT")
                .timestamp(Instant.now())
                .currentParticipantCount(activity.getCurrentParticipants())
                .maxParticipants(activity.getCapacityLimit())
                .build();

        sendToActivityParticipants(activity.getId(), ACTIVITY_PARTICIPANT_LEFT, participantUpdate);
    }

    // ===========================================
    // MESSAGING FEATURES
    // ===========================================

    /**
     * Broadcasts a message to all activity participants
     */
    public void broadcastMessageToActivity(Long activityId, String senderName, String message, String messageType) {
        log.info("Broadcasting message to activity {}: {}", activityId, messageType);

        ActivityChatMessageDto chatMessage = ActivityChatMessageDto.builder()
                .activityId(activityId)
                .senderName(senderName)
                .content(message)
                .messageType(ActivityChatMessageDto.MessageType.valueOf(messageType))
                .timestamp(Instant.now())
                .build();

        sendToActivityParticipants(activityId, ACTIVITY_MESSAGE_BROADCAST, chatMessage);
    }

    /**
     * Sends an announcement from organizer to all participants
     */
    public void sendActivityAnnouncement(EActivity activity, EAccountCredentials organizer, String announcement) {
        log.info("Sending activity announcement for activity {}: from organizer {}", 
                 activity.getId(), organizer.getId());

        ActivityChatMessageDto announcementMessage = ActivityChatMessageDto.builder()
                .activityId(activity.getId())
                .senderId(organizer.getId())
                .senderName(organizer.getFullName())
                .content(announcement)
                .messageType(ActivityChatMessageDto.MessageType.ANNOUNCEMENT)
                .timestamp(Instant.now())
                .build();

        sendToActivityParticipants(activity.getId(), ACTIVITY_ANNOUNCEMENT, announcementMessage);
    }

    /**
     * Sends emergency alert to all activity participants
     */
    public void sendEmergencyAlert(EActivity activity, String alertMessage, String alertType) {
        log.warn("Sending emergency alert for activity {}: {}", activity.getId(), alertType);

        EmergencyAlertDto emergencyAlert = EmergencyAlertDto.builder()
                .activityId(activity.getId())
                .activityName(activity.getActivityName())
                .alertType(alertType)
                .alertMessage(alertMessage)
                .timestamp(Instant.now())
                .severity("HIGH")
                .build();

        // Send to participants
        sendToActivityParticipants(activity.getId(), ACTIVITY_EMERGENCY_ALERT, emergencyAlert);
        
        // Send to organizers
        sendToActivityOrganizers(activity.getId(), ACTIVITY_EMERGENCY_ALERT, emergencyAlert);
    }

    // ===========================================
    // UTILITY METHODS FOR TARGETED SENDING
    // ===========================================

    /**
     * Sends a message to all participants of an activity
     */
    private void sendToActivityParticipants(Long activityId, String eventName, Object data) {
        CompletableFuture.runAsync(() -> {
            try {
                List<EParticipationDetail> participants = participationDetailRepository
                        .findByActivityIdAndStatus(activityId, ParticipationStatus.VERIFIED);

                int sentCount = 0;
                for (EParticipationDetail participation : participants) {
                    boolean sent = socketIOService.sendNotification(
                            participation.getParticipant().getId(), eventName, data);
                    if (sent) sentCount++;
                }

                log.debug("Sent {} event to {}/{} participants of activity {}", 
                         eventName, sentCount, participants.size(), activityId);

            } catch (Exception e) {
                log.error("Error sending to activity participants: {}", e.getMessage());
            }
        });
    }

    /**
     * Sends a message to all participants except specified user
     */
    private void sendToActivityParticipantsExcept(Long activityId, Long excludeUserId, String eventName, Object data) {
        CompletableFuture.runAsync(() -> {
            try {
                List<EParticipationDetail> participants = participationDetailRepository
                        .findByActivityIdAndStatus(activityId, ParticipationStatus.VERIFIED);

                int sentCount = 0;
                for (EParticipationDetail participation : participants) {
                    if (!participation.getParticipant().getId().equals(excludeUserId)) {
                        boolean sent = socketIOService.sendNotification(
                                participation.getParticipant().getId(), eventName, data);
                        if (sent) sentCount++;
                    }
                }

                log.debug("Sent {} event to {}/{} participants of activity {} (excluding user {})", 
                         eventName, sentCount, participants.size() - 1, activityId, excludeUserId);

            } catch (Exception e) {
                log.error("Error sending to activity participants: {}", e.getMessage());
            }
        });
    }

    /**
     * Sends a message to activity organizers
     */
    private void sendToActivityOrganizers(Long activityId, String eventName, Object data) {
        CompletableFuture.runAsync(() -> {
            try {
                EActivity activity = activityRepository.findById(activityId).orElse(null);
                if (activity != null && activity.getOrganization() != null) {
                    socketIOService.sendNotification(
                            activity.getOrganization().getId(), eventName, data);
                    log.debug("Sent {} event to organizer of activity {}", eventName, activityId);
                }
            } catch (Exception e) {
                log.error("Error sending to activity organizer: {}", e.getMessage());
            }
        });
    }

    // ===========================================
    // EVENT HANDLERS
    // ===========================================

    private void handleJoinActivityRoom(com.corundumstudio.socketio.SocketIOClient client, Map<String, Object> data, Object ackSender) {
        try {
            Long userId = getUserIdFromClient(client);
            Long activityId = Long.valueOf(data.get("activityId").toString());

            if (userId == null) {
                client.sendEvent("error", Map.of("message", "Authentication required"));
                return;
            }

            // Verify user is a participant
            boolean isParticipant = participationDetailRepository.existsByActivityIdAndParticipantIdAndStatus(
                    activityId, userId, ParticipationStatus.VERIFIED);

            if (!isParticipant) {
                client.sendEvent("join_activity_room_error", 
                        Map.of("message", "Not authorized to join this activity room"));
                return;
            }

            String roomName = ACTIVITY_ROOM_PREFIX + activityId;
            client.joinRoom(roomName);

            client.sendEvent("joined_activity_room", Map.of(
                    "activityId", activityId,
                    "roomName", roomName,
                    "timestamp", Instant.now()));

            log.debug("User {} joined activity room {}", userId, activityId);

        } catch (Exception e) {
            log.error("Error handling join activity room: {}", e.getMessage());
            client.sendEvent("join_activity_room_error", Map.of("message", "Failed to join room"));
        }
    }

    private void handleLeaveActivityRoom(com.corundumstudio.socketio.SocketIOClient client, Map<String, Object> data, Object ackSender) {
        try {
            Long activityId = Long.valueOf(data.get("activityId").toString());
            String roomName = ACTIVITY_ROOM_PREFIX + activityId;
            
            client.leaveRoom(roomName);
            
            client.sendEvent("left_activity_room", Map.of(
                    "activityId", activityId,
                    "timestamp", Instant.now()));

        } catch (Exception e) {
            log.error("Error handling leave activity room: {}", e.getMessage());
        }
    }

    private void handleGetActivityParticipants(com.corundumstudio.socketio.SocketIOClient client, Map<String, Object> data, Object ackSender) {
        try {
            Long activityId = Long.valueOf(data.get("activityId").toString());
            
            List<EParticipationDetail> participants = participationDetailRepository
                    .findByActivityIdAndStatus(activityId, ParticipationStatus.VERIFIED);

            List<Map<String, Object>> participantList = participants.stream()
                    .map(p -> {
                        Map<String, Object> participantInfo = new HashMap<>();
                        participantInfo.put("id", p.getParticipant().getId());
                        participantInfo.put("name", p.getParticipant().getFullName());
                        participantInfo.put("email", p.getParticipant().getEmail());
                        participantInfo.put("isOnline", socketCacheService.isUserOnline(p.getParticipant().getId()));
                        return participantInfo;
                    })
                    .collect(Collectors.toList());

            client.sendEvent("activity_participants_list", Map.of(
                    "activityId", activityId,
                    "participants", participantList,
                    "count", participantList.size()));

        } catch (Exception e) {
            log.error("Error getting activity participants: {}", e.getMessage());
        }
    }

    private void handleActivityBroadcast(com.corundumstudio.socketio.SocketIOClient client, ActivityChatMessageDto messageDto, Object ackSender) {
        try {
            Long userId = getUserIdFromClient(client);
            if (userId == null || !userId.equals(messageDto.getSenderId())) {
                client.sendEvent("broadcast_error", Map.of("message", "Invalid sender"));
                return;
            }

            // Verify sender has permission (is participant or organizer)
            EActivity activity = activityRepository.findById(messageDto.getActivityId()).orElse(null);
            if (activity == null) {
                client.sendEvent("broadcast_error", Map.of("message", "Activity not found"));
                return;
            }

            boolean isAuthorized = participationDetailRepository.existsByActivityIdAndParticipantIdAndStatus(
                    messageDto.getActivityId(), userId, ParticipationStatus.VERIFIED) ||
                    activity.getOrganization().getId().equals(userId);

            if (!isAuthorized) {
                client.sendEvent("broadcast_error", Map.of("message", "Not authorized"));
                return;
            }

            messageDto.setTimestamp(Instant.now());
            sendToActivityParticipants(messageDto.getActivityId(), ACTIVITY_MESSAGE_BROADCAST, messageDto);

            client.sendEvent("broadcast_success", Map.of(
                    "messageId", messageDto.getMessageId(),
                    "timestamp", Instant.now()));

        } catch (Exception e) {
            log.error("Error handling activity broadcast: {}", e.getMessage());
            client.sendEvent("broadcast_error", Map.of("message", "Failed to broadcast message"));
        }
    }

    private void handleEmergencyAlert(com.corundumstudio.socketio.SocketIOClient client, Map<String, Object> data, Object ackSender) {
        try {
            Long userId = getUserIdFromClient(client);
            Long activityId = Long.valueOf(data.get("activityId").toString());
            String alertMessage = data.get("message").toString();
            String alertType = data.getOrDefault("alertType", "GENERAL").toString();

            // Verify sender is organizer
            EActivity activity = activityRepository.findById(activityId).orElse(null);
            if (activity == null || !activity.getOrganization().getId().equals(userId)) {
                client.sendEvent("emergency_alert_error", Map.of("message", "Not authorized"));
                return;
            }

            sendEmergencyAlert(activity, alertMessage, alertType);

            client.sendEvent("emergency_alert_sent", Map.of(
                    "activityId", activityId,
                    "timestamp", Instant.now()));

        } catch (Exception e) {
            log.error("Error handling emergency alert: {}", e.getMessage());
            client.sendEvent("emergency_alert_error", Map.of("message", "Failed to send alert"));
        }
    }

    private void handleSubscribeActivityUpdates(com.corundumstudio.socketio.SocketIOClient client, Map<String, Object> data, Object ackSender) {
        try {
            Long userId = getUserIdFromClient(client);
            Long activityId = Long.valueOf(data.get("activityId").toString());

            // For now, just acknowledge the subscription (could implement actual subscription tracking later)
            log.debug("User {} subscribed to activity {} updates", userId, activityId);

            client.sendEvent("subscribed_to_activity", Map.of(
                    "activityId", activityId,
                    "timestamp", Instant.now()));

        } catch (Exception e) {
            log.error("Error subscribing to activity updates: {}", e.getMessage());
        }
    }

    private void handleUnsubscribeActivityUpdates(com.corundumstudio.socketio.SocketIOClient client, Map<String, Object> data, Object ackSender) {
        try {
            Long userId = getUserIdFromClient(client);
            Long activityId = Long.valueOf(data.get("activityId").toString());

            // For now, just acknowledge the unsubscription (could implement actual subscription tracking later)
            log.debug("User {} unsubscribed from activity {} updates", userId, activityId);

            client.sendEvent("unsubscribed_from_activity", Map.of(
                    "activityId", activityId,
                    "timestamp", Instant.now()));

        } catch (Exception e) {
            log.error("Error unsubscribing from activity updates: {}", e.getMessage());
        }
    }

    // ===========================================
    // HELPER METHODS
    // ===========================================

    private Long getUserIdFromClient(com.corundumstudio.socketio.SocketIOClient client) {
        try {
            String userIdParam = client.getHandshakeData().getSingleUrlParam("userId");
            return userIdParam != null ? Long.valueOf(userIdParam) : null;
        } catch (Exception e) {
            log.error("Error extracting user ID from client: {}", e.getMessage());
            return null;
        }
    }

    private void sendActivityWelcomeMessage(EActivity activity, EAccountCredentials participant) {
        SocketNotificationDto welcomeMessage = SocketNotificationDto.builder()
                .title("Welcome to " + activity.getActivityName() + "!")
                .message("You have successfully joined this activity. Stay tuned for updates and announcements.")
                .type(NotificationType.ACTIVITY)
                .activityId(activity.getId())
                .timestamp(Instant.now())
                .activityName(activity.getActivityName())
                .activityStartDate(activity.getStartDate())
                .build();

        socketIOService.sendNotification(participant.getId(), "activity_welcome", welcomeMessage);
    }

    private String getActivityReminderTitle(long daysUntilStart) {
        if (daysUntilStart == 0) return "Activity Starting Today!";
        if (daysUntilStart == 1) return "Activity Starting Tomorrow!";
        return "Activity Starting in " + daysUntilStart + " Days!";
    }

    private String getActivityReminderMessage(EActivity activity, long daysUntilStart) {
        if (daysUntilStart == 0) {
            return "Your activity '" + activity.getActivityName() + "' is starting today!";
        } else if (daysUntilStart == 1) {
            return "Your activity '" + activity.getActivityName() + "' starts tomorrow!";
        } else {
            return "Your activity '" + activity.getActivityName() + "' starts in " + daysUntilStart + " days!";
        }
    }

    // ===========================================
    // PUBLIC API METHODS FOR OTHER SERVICES
    // ===========================================

    /**
     * Public method for other services to broadcast activity status changes
     */
    public void notifyActivityStatusChange(Long activityId, ActivityStatus oldStatus, ActivityStatus newStatus) {
        EActivity activity = activityRepository.findById(activityId).orElse(null);
        if (activity != null) {
            broadcastActivityStatusChange(activity, oldStatus, newStatus);
        }
    }

    /**
     * Public method for other services to send activity reminders
     */
    public void notifyActivityReminder(Long activityId, long daysUntilStart) {
        EActivity activity = activityRepository.findById(activityId).orElse(null);
        if (activity != null) {
            sendActivityReminder(activity, daysUntilStart);
        }
    }

    /**
     * Public method for other services to broadcast participant changes
     */
    public void notifyParticipantJoined(Long activityId, Long participantId) {
        EActivity activity = activityRepository.findById(activityId).orElse(null);
        EAccountCredentials participant = accountRepository.findById(participantId).orElse(null);
        
        if (activity != null && participant != null) {
            broadcastParticipantJoined(activity, participant);
        }
    }

    /**
     * Public method for other services to broadcast participant leaving
     */
    public void notifyParticipantLeft(Long activityId, Long participantId) {
        EActivity activity = activityRepository.findById(activityId).orElse(null);
        EAccountCredentials participant = accountRepository.findById(participantId).orElse(null);
        
        if (activity != null && participant != null) {
            broadcastParticipantLeft(activity, participant);
        }
    }
}
