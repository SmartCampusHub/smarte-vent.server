package com.winnguyen1905.activity.model.dto;

import java.time.Instant;
import java.util.List;

import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto implements AbstractModel {
  private Long id;
  private String activityName;
  private String description;
  private Instant startDate;
  private Instant endDate;
  private String activityVenue;
  private Integer capacityLimit;
  private String activityType;
  private ActivityCategory activityCategory;
  private String activityDescription;
  private String imageUrl;
  private String activityLink;
  private Integer attendanceScoreUnit;
  private List<ActivityScheduleDto> activitySchedules;

  private String shortDescription;
  private List<String> tags;
  private Integer currentParticipants;
  private String address;
  private Double latitude;
  private Double longitude;
  private Double fee;
  private Boolean isFeatured;
  private Boolean isApproved;
  private int likes;
  private Instant registrationDeadline;
}
