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
public class StudentStatisticsVm implements AbstractModel {
  // Personal identification
  private Long studentId;
  private String studentName;

  // Participation statistics
  private Long totalActivitiesParticipated;
  private Long activitiesAsVolunteer;
  private Long activitiesAsParticipant;

  // Time-based statistics
  private Double totalParticipationHours;

  // Performance statistics
  private Double averageAssessmentScore;
  private Double totalTrainingScore;

  // Activity type breakdown
  private Map<String, Long> activitiesByCategory;

  // Recent activity history
  private List<ParticipationSummaryVm> recentActivities;

  // Progress trends (month by month)
  private Map<String, Long> monthlyParticipationTrend;
}
