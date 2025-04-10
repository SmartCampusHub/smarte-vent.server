package com.winnguyen1905.Activity.model.dto;

import java.time.Instant;

import com.winnguyen1905.Activity.common.constant.ParticipationRole;

public record ParticipationDetailDto(
    Long activityId,
    ParticipationRole role) implements AbstractModel{
}
