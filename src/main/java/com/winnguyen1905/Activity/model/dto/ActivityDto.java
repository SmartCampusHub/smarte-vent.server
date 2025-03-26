package com.winnguyen1905.Activity.model.dto;

import java.time.LocalDateTime;

import com.winnguyen1905.Activity.common.constant.ActivityStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDto {
  private String activityName;
  private String description;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String activityVenue;
  private Integer capacity;
  private ActivityStatus activityStatus;
  private String attendanceScoreUnit;
  private Long activityCategoryId;
  private Long representativeOrganizerId;
}
