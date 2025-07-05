package com.winnguyen1905.activity.websocket.dto;

import com.winnguyen1905.activity.rest.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for activity update notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateDto implements AbstractModel {
    private Long activityId;
    private String activityName;
    private String updateType; // TIME_CHANGE, LOCATION_CHANGE, DETAILS_CHANGE, etc.
    private String updateMessage;
    private Instant timestamp;
    private Instant startDate;
    private Instant endDate;
    private String venue;
} 
