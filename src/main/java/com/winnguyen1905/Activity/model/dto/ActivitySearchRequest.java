package com.winnguyen1905.Activity.model.dto;

import java.time.Instant;
import java.util.List;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;

import lombok.Builder;

@Builder
public record ActivitySearchRequest(
    String activityName,
    ActivityCategory activityCategory,
    ActivityStatus activityStatus,
    String organizationName,
    Instant startDateFrom,
    Instant startDateTo,
    Instant endDateFrom,
    Instant endDateTo,
    Integer minAttendanceScoreUnit,
    Integer maxAttendanceScoreUnit,
    Integer minCapacityLimit,
    Integer maxCapacityLimit,
    String activityVenue,

    String shortDescription,
    List<String> tags,
    Integer currentParticipants,
    String address,
    Double latitude,
    Double longitude,
    Double fee,
    Boolean isFeatured,
    Boolean isApproved,
    Integer likes,
    Instant registrationDeadline) implements AbstractModel {
  @Builder
  public ActivitySearchRequest(
      String activityName,
      ActivityCategory activityCategory,
      ActivityStatus activityStatus,
      String organizationName,
      Instant startDateFrom,
      Instant startDateTo,
      Instant endDateFrom,
      Instant endDateTo,
      Integer minAttendanceScoreUnit,
      Integer maxAttendanceScoreUnit,
      Integer minCapacityLimit,
      Integer maxCapacityLimit,
      String activityVenue,

      String shortDescription,
      List<String> tags,
      Integer currentParticipants,
      String address,
      Double latitude,
      Double longitude,
      Double fee,
      Boolean isFeatured,
      Boolean isApproved,
      Integer likes,
      Instant registrationDeadline) {
    this.activityName = activityName;
    this.activityCategory = activityCategory;
    this.activityStatus = activityStatus;
    this.organizationName = organizationName;
    this.startDateFrom = startDateFrom;
    this.startDateTo = startDateTo;
    this.endDateFrom = endDateFrom;
    this.endDateTo = endDateTo;
    this.minAttendanceScoreUnit = minAttendanceScoreUnit;
    this.maxAttendanceScoreUnit = maxAttendanceScoreUnit;
    this.minCapacityLimit = minCapacityLimit;
    this.maxCapacityLimit = maxCapacityLimit;
    this.activityVenue = activityVenue;
    this.shortDescription = shortDescription;
    this.tags = tags;
    this.currentParticipants = currentParticipants;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    this.fee = fee;
    this.isFeatured = isFeatured;
    this.isApproved = isApproved;
    this.likes = likes;
    this.registrationDeadline = registrationDeadline;
  }

}
