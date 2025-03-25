package com.winnguyen1905.Activity.model.dto;

import java.io.Serializable;
import java.util.UUID;

public record NotificationDto(
    UUID receiverId, String title, String content) implements Serializable {
}
