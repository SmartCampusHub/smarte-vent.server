package com.winnguyen1905.activity.websocket.dto;

import com.winnguyen1905.activity.common.constant.NotificationType;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for Socket.IO notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketNotificationDto implements AbstractModel {
    private String title;
    private String message;
    private NotificationType type;
    private Long activityId;
    private Instant timestamp;
    private Long daysUntilStart;
    private String activityName;
    private Instant activityStartDate;
} 
