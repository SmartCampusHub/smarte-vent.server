package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.winnguyen1905.activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarActivityMetricsVm implements AbstractModel {
  private Long activityId;
  private String activityName;
  private String activityCategory;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant startDate;

  // Similarity metrics
  private Double similarityScore; // How similar this activity is to the reference activity

  // Performance metrics
  private Integer participantCount;
  private Double participationRate;
  private Double averageRating;
  private Double costPerParticipant;

  // Comparative metrics
  private Double participantCountDifference; // % difference from reference activity
  private Double participationRateDifference; // % difference from reference activity
  private Double averageRatingDifference; // % difference from reference activity
  private Double costPerParticipantDifference; // % difference from reference activity

  // Success factors
  private String successFactors; // Factors that may contribute to better/worse performance
}
