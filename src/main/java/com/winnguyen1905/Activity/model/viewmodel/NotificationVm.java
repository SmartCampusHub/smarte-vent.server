package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.common.constant.NotificationType;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationVm implements AbstractModel {
  private Long id;
  private Long receiverId;
  private Boolean isRead;
  private String title;
  private String content;
  private NotificationType notificationType;
  private String createdBy;
  private Instant createdDate;
}
