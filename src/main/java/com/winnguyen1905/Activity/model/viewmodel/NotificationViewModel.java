package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.common.constant.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationViewModel {
    private Long id;
    private Long receiverId;
    private String message;
    private NotificationType notificationType;
    private String createdBy;
    private Instant createdDate;
}
