package com.winnguyen1905.Activity.rest.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.model.viewmodel.ActivityComparativeAnalysisVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityStatisticsVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityTimeSeriesVm;
import com.winnguyen1905.Activity.model.viewmodel.SimilarActivityMetricsVm;
import com.winnguyen1905.Activity.rest.service.ActivityStatisticsService;

@RestController
@RequestMapping("/api/activity-statistics")
public class ActivityStatisticsController {

  @Autowired
  private ActivityStatisticsService activityStatisticsService;

  /**
   * Get comprehensive statistics for a specific activity
   * 
   * @param activityId The ID of the activity to analyze
   * @return ActivityStatisticsVm containing detailed statistics
   */
  @GetMapping("/{activityId}")
  public ResponseEntity<ActivityStatisticsVm> getActivityStatistics(
      @PathVariable("activityId") Long activityId) {
    ActivityStatisticsVm statistics = activityStatisticsService.getActivityStatistics(activityId);
    return ResponseEntity.ok(statistics);
  }

  /**
   * Get statistics for an activity within a specific time range
   * 
   * @param activityId The ID of the activity to analyze
   * @param startDate  Start date of the time range
   * @param endDate    End date of the time range
   * @return ActivityStatisticsVm containing filtered statistics
   */
  @GetMapping("/{activityId}/time-range")
  public ResponseEntity<ActivityStatisticsVm> getActivityStatisticsInTimeRange(
      @PathVariable("activityId") Long activityId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
    ActivityStatisticsVm statistics = activityStatisticsService.getActivityStatisticsInTimeRange(activityId, startDate,
        endDate);
    return ResponseEntity.ok(statistics);
  }

  /**
   * Get participation trend data for an activity
   * 
   * @param activityId The ID of the activity to analyze
   * @return ActivityStatisticsVm with participation trend data
   */
  @GetMapping("/{activityId}/participation-trend")
  public ResponseEntity<ActivityStatisticsVm> getParticipationTrend(
      @PathVariable("activityId") Long activityId) {
    ActivityStatisticsVm statistics = activityStatisticsService.getParticipationTrend(activityId);
    return ResponseEntity.ok(statistics);
  }

  /**
   * Get feedback analysis for an activity
   * 
   * @param activityId The ID of the activity to analyze
   * @return ActivityStatisticsVm with feedback metrics
   */
  @GetMapping("/{activityId}/feedback-analysis")
  public ResponseEntity<ActivityStatisticsVm> getFeedbackAnalysis(
      @PathVariable("activityId") Long activityId) {
    ActivityStatisticsVm statistics = activityStatisticsService.getFeedbackAnalysis(activityId);
    return ResponseEntity.ok(statistics);
  }

  /**
   * Get detailed participant performance for an activity
   * 
   * @param activityId The ID of the activity to analyze
   * @return ActivityStatisticsVm with detailed participant information
   */
  @GetMapping("/{activityId}/participant-performance")
  public ResponseEntity<ActivityStatisticsVm> getParticipantPerformance(
      @PathVariable("activityId") Long activityId) {
    ActivityStatisticsVm statistics = activityStatisticsService.getParticipantPerformance(activityId);
    return ResponseEntity.ok(statistics);
  }

  /**
   * Get comparative analysis for an activity compared to similar activities
   * 
   * @param activityId The ID of the activity to analyze
   * @return Comparative metrics and benchmarking against similar activities
   */
  @GetMapping("/{activityId}/comparative-analysis")
  public ResponseEntity<ActivityComparativeAnalysisVm> getComparativeAnalysis(
      @PathVariable("activityId") Long activityId) {
    ActivityComparativeAnalysisVm analysis = activityStatisticsService.getComparativeAnalysis(activityId);
    return ResponseEntity.ok(analysis);
  }

  /**
   * Get time series analysis for an activity
   * 
   * @param activityId The ID of the activity to analyze
   * @return Time-based metrics and trends
   */
  @GetMapping("/{activityId}/time-series")
  public ResponseEntity<ActivityTimeSeriesVm> getTimeSeriesAnalysis(
      @PathVariable("activityId") Long activityId) {
    ActivityTimeSeriesVm timeSeries = activityStatisticsService.getTimeSeriesAnalysis(activityId);
    return ResponseEntity.ok(timeSeries);
  }

  /**
   * Get effectiveness metrics for an activity including ROI, cost per participant
   * 
   * @param activityId     The ID of the activity to analyze
   * @param estimatedCost  Optional estimated cost if not stored in system
   * @param estimatedValue Optional estimated value if not stored in system
   * @return Effectiveness metrics including ROI calculations
   */
  @GetMapping("/{activityId}/effectiveness-metrics")
  public ResponseEntity<ActivityComparativeAnalysisVm> getEffectivenessMetrics(
      @PathVariable("activityId") Long activityId,
      @RequestParam(required = false) Double estimatedCost,
      @RequestParam(required = false) Double estimatedValue) {
    ActivityComparativeAnalysisVm effectiveness = activityStatisticsService.getEffectivenessMetrics(
        activityId, estimatedCost, estimatedValue);
    return ResponseEntity.ok(effectiveness);
  }

  /**
   * Find similar activities to the specified activity
   * 
   * @param activityId The ID of the activity to find similar activities for
   * @param limit      Maximum number of similar activities to return (default 5)
   * @return List of similar activities with similarity scores and metrics
   */
  @GetMapping("/{activityId}/similar-activities")
  public ResponseEntity<List<SimilarActivityMetricsVm>> findSimilarActivities(
      @PathVariable("activityId") Long activityId,
      @RequestParam(required = false, defaultValue = "5") Integer limit) {
    List<SimilarActivityMetricsVm> similarActivities = activityStatisticsService.findSimilarActivities(activityId,
        limit);
    return ResponseEntity.ok(similarActivities);
  }

  /**
   * Get improvement recommendations for an activity based on historical data
   * 
   * @param activityId The ID of the activity to analyze
   * @return Improvement opportunities and recommendations
   */
  @GetMapping("/{activityId}/improvement-recommendations")
  public ResponseEntity<ActivityComparativeAnalysisVm> getImprovementRecommendations(
      @PathVariable("activityId") Long activityId) {
    ActivityComparativeAnalysisVm recommendations = activityStatisticsService.getImprovementRecommendations(activityId);
    return ResponseEntity.ok(recommendations);
  }
}
