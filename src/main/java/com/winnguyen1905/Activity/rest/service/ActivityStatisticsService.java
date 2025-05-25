package com.winnguyen1905.Activity.rest.service;

import java.time.Instant;
import java.util.List;

import com.winnguyen1905.Activity.model.viewmodel.ActivityComparativeAnalysisVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityStatisticsVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityTimeSeriesVm;
import com.winnguyen1905.Activity.model.viewmodel.SimilarActivityMetricsVm;

public interface ActivityStatisticsService {
    
    /**
     * Gets comprehensive statistics for a specific activity
     * 
     * @param activityId The ID of the activity to analyze
     * @return ActivityStatisticsVm containing detailed statistics
     */
    ActivityStatisticsVm getActivityStatistics(Long activityId);
    
    /**
     * Gets statistics for an activity within a specific time range
     * 
     * @param activityId The ID of the activity to analyze
     * @param startDate Start date of the time range
     * @param endDate End date of the time range
     * @return ActivityStatisticsVm containing filtered statistics
     */
    ActivityStatisticsVm getActivityStatisticsInTimeRange(Long activityId, Instant startDate, Instant endDate);
    
    /**
     * Gets participation trend data for an activity
     * 
     * @param activityId The ID of the activity to analyze
     * @return Map of dates to participation counts
     */
    ActivityStatisticsVm getParticipationTrend(Long activityId);
    
    /**
     * Gets feedback analysis for an activity
     * 
     * @param activityId The ID of the activity to analyze
     * @return ActivityStatisticsVm with focus on feedback metrics
     */
    ActivityStatisticsVm getFeedbackAnalysis(Long activityId);
    
    /**
     * Gets detailed participant performance for an activity
     * 
     * @param activityId The ID of the activity to analyze
     * @return ActivityStatisticsVm with detailed participant information
     */
    ActivityStatisticsVm getParticipantPerformance(Long activityId);
    
    /**
     * Gets comparative analysis for an activity compared to similar activities
     * 
     * @param activityId The ID of the activity to analyze
     * @return ActivityComparativeAnalysisVm containing comparative metrics
     */
    ActivityComparativeAnalysisVm getComparativeAnalysis(Long activityId);
    
    /**
     * Gets time series analysis for an activity 
     * 
     * @param activityId The ID of the activity to analyze
     * @return ActivityTimeSeriesVm containing time-based metrics
     */
    ActivityTimeSeriesVm getTimeSeriesAnalysis(Long activityId);
    
    /**
     * Gets effectiveness metrics for an activity including ROI, cost per participant
     * 
     * @param activityId The ID of the activity to analyze
     * @param estimatedCost The estimated cost of the activity (if not stored in system)
     * @param estimatedValue The estimated value generated (if not stored in system)
     * @return ActivityComparativeAnalysisVm with focus on effectiveness metrics
     */
    ActivityComparativeAnalysisVm getEffectivenessMetrics(Long activityId, Double estimatedCost, Double estimatedValue);
    
    /**
     * Finds similar activities to the specified activity
     * 
     * @param activityId The ID of the activity to find similar activities for
     * @param limit Maximum number of similar activities to return
     * @return List of SimilarActivityMetricsVm with similarity scores and metrics
     */
    List<SimilarActivityMetricsVm> findSimilarActivities(Long activityId, Integer limit);
    
    /**
     * Gets improvement recommendations for an activity based on historical data
     * 
     * @param activityId The ID of the activity to analyze
     * @return ActivityComparativeAnalysisVm with focus on improvement opportunities
     */
    ActivityComparativeAnalysisVm getImprovementRecommendations(Long activityId);
}
