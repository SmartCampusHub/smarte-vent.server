package com.winnguyen1905.activity.rest.model.dto;

import java.io.Serializable;
import java.util.UUID;

import com.winnguyen1905.activity.common.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto implements AbstractModel {
  private Long receiverId;
  private String title;
  private String content;
  private NotificationType notificationType;
}
