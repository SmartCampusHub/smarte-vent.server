package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVm implements AbstractModel {
  private Long id;
  private String activityName;
  private String description;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant startDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant endDate;
  private String activityVenue;
  private Integer capacityLimit;
  private ActivityStatus activityStatus;
  private String activityType;
  private ActivityCategory activityCategory;
  private String activityDescription;
  private String activityImage;
  private String activityLink;
  private String attendanceScoreUnit;
  private Long representativeOrganizerId;
  private List<ActivityScheduleVm> activitySchedules;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant createdDate;
  private OrganizationVm organization;
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
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant registrationDeadline;
}
