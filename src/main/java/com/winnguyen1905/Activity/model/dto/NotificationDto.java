package com.winnguyen1905.Activity.model.dto;

import java.io.Serializable;
import java.util.UUID;

import com.winnguyen1905.Activity.common.constant.NotificationType;

import lombok.Builder;

@Builder
public record NotificationDto(
        Long receiverId, String title, String content, NotificationType notificationType) implements AbstractModel {

    @Builder
    public NotificationDto(Long receiverId, String title, String content, NotificationType notificationType) {
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
    }
}
