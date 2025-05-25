package com.winnguyen1905.Activity.model.viewmodel;

import java.util.List;
import java.util.Map;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityComparativeAnalysisVm implements AbstractModel {
    private Long activityId;
    private String activityName;
    
    // Comparative metrics
    private Double averageRatingVsCategoryAverage; // How this activity's rating compares to category average
    private Double participationRateVsCategoryAverage; // How this activity's participation rate compares
    private Double costPerParticipantVsCategoryAverage; // Cost effectiveness comparison
    
    // Historical comparison (this activity's previous instances)
    private List<PreviousRunMetricsVm> previousRunsComparison;
    
    // Similar activities comparison
    private List<SimilarActivityMetricsVm> similarActivitiesComparison;
    
    // Percentile rankings within category
    private Integer participationPercentile; // Percentile rank for participation rate
    private Integer ratingPercentile; // Percentile rank for rating
    private Integer engagementPercentile; // Percentile rank for engagement metrics
    
    // Effectiveness metrics
    private Double returnOnInvestment; // (Value generated / Cost) * 100
    private Double costPerParticipant; // Total cost / Number of participants
    private Double valuePerParticipant; // Estimated value generated per participant
    
    // Improvement metrics
    private Map<String, Double> improvementOpportunities; // Areas with potential for improvement with scores
}
