package com.winnguyen1905.activity.model.viewmodel;

import java.util.List;
import java.util.Map;

import com.winnguyen1905.activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationStatisticsVm implements AbstractModel {
  // Organization identification
  private Long organizationId;
  private String organizationName;
  private String organizationType;

  // Activity statistics
  private Long totalActivities;
  private Long upcomingActivities;
  private Long ongoingActivities;
  private Long completedActivities;
  private Long canceledActivities;

  // Participation statistics
  private Long totalParticipants;
  private Double averageParticipantsPerActivity;
  private Double participationRate; // % of available slots filled

  // Performance metrics
  private Double averageFeedbackRating;
  private Long totalFeedbacks;

  // Category breakdown
  private Map<String, Long> activitiesByCategory;
  private Map<String, Long> participantsByCategory;

  // Time-based metrics
  private Map<String, Long> activitiesByMonth;
  private Map<String, Long> participantsByMonth;

  // Most popular activities (by participation)
  private List<ActivityStatisticsSummaryVm> topActivities;

  // Best rated activities
  private List<ActivityStatisticsSummaryVm> bestRatedActivities;
}
