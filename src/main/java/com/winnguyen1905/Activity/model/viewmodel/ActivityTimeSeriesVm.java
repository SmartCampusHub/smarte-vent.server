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
public class ActivityTimeSeriesVm  implements AbstractModel {
    private Long activityId;
    private String activityName;
    
    // Registration trend (daily/hourly counts up to activity start)
    private Map<String, Integer> registrationTimeSeries;
    
    // Feedback trend (rating over time after activity)
    private Map<String, Double> feedbackTimeSeries;
    
    // Engagement metrics over time (for multi-day activities)
    private Map<String, Double> engagementTimeSeries;
    
    // Social media mentions/interactions (if tracked)
    private Map<String, Integer> socialInteractionTimeSeries;
    
    // Seasonal analysis (is this activity more successful in certain times of year?)
    private List<SeasonalPerformanceVm> seasonalPerformance;
    
    // Time-to-capacity metric (how quickly did registrations reach capacity?)
    private Integer timeToCapacityHours;
    private Double timeToCapacityPercentOfAverage; // Compared to similar activities
    
    // Peak registration times (useful for marketing future activities)
    private List<String> peakRegistrationTimeSlots;
}
