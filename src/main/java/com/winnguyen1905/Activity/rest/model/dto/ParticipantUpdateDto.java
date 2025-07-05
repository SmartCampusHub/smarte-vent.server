package com.winnguyen1905.activity.rest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for participant update notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantUpdateDto implements AbstractModel {
    private Long activityId;
    private String activityName;
    private Long participantId;
    private String participantName;
    private String participantEmail;
    private String updateType; // JOINED, LEFT
    private Instant timestamp;
    private Integer currentParticipantCount;
    private Integer maxParticipants;
} 
