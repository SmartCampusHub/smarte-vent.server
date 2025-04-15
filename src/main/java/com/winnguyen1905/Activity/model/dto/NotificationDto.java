package com.winnguyen1905.Activity.model.dto;

import java.io.Serializable;
import java.util.UUID;

import com.winnguyen1905.Activity.common.constant.NotificationType;

public record NotificationDto(
    Long receiverId, String title, String content, NotificationType notificationType) implements Serializable {
}
