package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeasonalPerformanceVm  implements AbstractModel {
    private String season; // e.g., "Spring 2025", "Q1 2025"
    private Double participationRate;
    private Double averageRating;
    private Integer participantCount;
    private Double engagementScore;
    
    // Comparison to overall average
    private Double participationRateVsAverage; // % difference from yearly average
    private Double ratingVsAverage; // % difference from yearly average
    private Double participantCountVsAverage; // % difference from yearly average
}
