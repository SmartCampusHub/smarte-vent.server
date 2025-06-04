package com.winnguyen1905.Activity.model.dto;

import java.time.Instant;

import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;

public record ParticipationSearchParams(
    Long activityId,
    Long participantId,
    String participantName,
    String identifyCode,
    ParticipationStatus participationStatus,
    ParticipationRole participationRole,
    Instant registeredAfter,
    Instant registeredBefore) implements AbstractModel {
}
