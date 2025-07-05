package com.winnguyen1905.activity.websocket.dto;

import com.winnguyen1905.activity.rest.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for emergency alert notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlertDto implements AbstractModel {
    private Long activityId;
    private String activityName;
    private String alertType; // WEATHER, SECURITY, MEDICAL, GENERAL
    private String alertMessage;
    private Instant timestamp;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
} 
