package com.winnguyen1905.Activity.model.dto;

import java.time.Instant;

import com.winnguyen1905.Activity.common.constant.ParticipationRole;

public record ParticipationDetailDto(
    String studentId,
    Long activityId,
    ParticipationRole role) {
}
