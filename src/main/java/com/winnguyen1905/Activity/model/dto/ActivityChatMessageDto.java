package com.winnguyen1905.activity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for activity chat messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityChatMessageDto implements AbstractModel {
    private Long messageId;
    private Long activityId;
    private String activityName;
    private Long senderId;
    private String senderName;
    private String senderRole; // ORGANIZER, PARTICIPANT, etc.
    private String content;
    private MessageType messageType;
    private Instant timestamp;
    private Boolean isPinned;
    private String attachmentUrl;
    private Long replyToMessageId;
    
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        ANNOUNCEMENT,
        SYSTEM_MESSAGE,
        POLL,
        REMINDER
    }
} 
