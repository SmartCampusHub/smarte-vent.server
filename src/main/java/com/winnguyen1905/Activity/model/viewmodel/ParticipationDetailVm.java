package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;

import lombok.Builder;

@Builder
public record ParticipationDetailVm(
    Long id,
    Long studentId,
    Long activityId,
    String activityName,
    ActivityCategory activityCategory,
    String activityVenue,
    Instant startDate,
    Instant endDate,
    Instant registrationTime,
    ActivityStatus activityStatus,
    ParticipationStatus participationStatus,
    ParticipationRole participationRole) {
}
