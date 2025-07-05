package com.winnguyen1905.activity.websocket.dto;

import com.winnguyen1905.activity.rest.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for chat messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto implements AbstractModel {
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String content;
    private MessageType messageType;
    private Instant timestamp;
    private Boolean isRead;
    private String attachmentUrl;
    private Long activityId; // For activity-related messages
    private Long replyToMessageId; // For reply functionality
    
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM_NOTIFICATION,
        ACTIVITY_ANNOUNCEMENT
    }
} 
