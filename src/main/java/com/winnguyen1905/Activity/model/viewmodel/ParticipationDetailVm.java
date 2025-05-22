package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

public record ParticipationDetailVm(
    Long id,
    Long studentId,
    String studentCode,
    String participantName,
    Long activityId,
    String activityName,
    ActivityCategory activityCategory,
    String activityVenue, @JsonFormat(shape = JsonFormat.Shape.STRING)

    Instant startDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant endDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant registrationTime,
    ActivityStatus activityStatus,
    ParticipationStatus participationStatus,
    ParticipationRole participationRole) implements AbstractModel {

  @Builder
  public ParticipationDetailVm(
      Long id,
      Long studentId,
      String studentCode,
      String participantName,
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
    this.id = id;
    this.studentId = studentId;
    this.studentCode = studentCode;
    this.participantName = participantName;
    this.activityId = activityId;
    this.activityName = activityName;
    this.activityCategory = activityCategory;
    this.activityVenue = activityVenue;
    this.startDate = startDate;
    this.endDate = endDate;
    this.registrationTime = registrationTime;
    this.activityStatus = activityStatus;
    this.participationStatus = participationStatus;
    this.participationRole = participationRole;
  }
}
