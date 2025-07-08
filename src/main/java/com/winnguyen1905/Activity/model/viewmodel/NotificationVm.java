package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.winnguyen1905.activity.common.constant.NotificationType;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVm implements AbstractModel {
    private Long id;
    private String title;
    private String content;
    private Boolean isRead;
    private NotificationType notificationType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdDate;
} 
