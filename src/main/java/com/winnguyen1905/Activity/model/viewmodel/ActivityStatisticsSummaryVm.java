package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStatisticsSummaryVm implements AbstractModel {
  // Activity identification
  private Long activityId;
  private String activityName;
  private ActivityCategory category;
  private ActivityStatus status;

  // Time information
  private Instant startDate;
  private Instant endDate;

  // Participation metrics
  private Integer capacityLimit;
  private Integer currentParticipants;
  private Double participationRate; // % of capacity filled

  // Performance metrics
  private Double averageRating;
  private Long feedbackCount;
}
