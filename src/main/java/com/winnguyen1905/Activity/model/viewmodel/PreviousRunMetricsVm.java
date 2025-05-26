package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousRunMetricsVm implements AbstractModel {
  private Long activityId;
  private String activityName;
  private Instant startDate;
  private Instant endDate;

  // Key metrics for comparison
  private Integer participantCount;
  private Double participationRate;
  private Double averageRating;
  private Double costPerParticipant;

  // Change metrics
  private Double participantCountChange; // % change from this run
  private Double participationRateChange; // % change from this run
  private Double averageRatingChange; // % change from this run
  private Double costPerParticipantChange; // % change from this run
}
