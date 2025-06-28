package com.winnguyen1905.activity.rest.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.model.viewmodel.ActivityComparativeAnalysisVm;
import com.winnguyen1905.activity.model.viewmodel.ActivityStatisticsVm;
import com.winnguyen1905.activity.model.viewmodel.ActivityTimeSeriesVm;
import com.winnguyen1905.activity.model.viewmodel.ParticipantScoreVm;
import com.winnguyen1905.activity.model.viewmodel.PreviousRunMetricsVm;
import com.winnguyen1905.activity.model.viewmodel.SeasonalPerformanceVm;
import com.winnguyen1905.activity.model.viewmodel.SimilarActivityMetricsVm;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EFeedback;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.rest.service.ActivityStatisticsService;

@Service
public class ActivityStatisticsServiceImpl implements ActivityStatisticsService {

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private FeedbackRepository feedbackRepository;

  @Autowired
  private ParticipationDetailRepository participationDetailRepository;

  @Override
  public ActivityStatisticsVm getActivityStatistics(Long activityId) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();
    List<EParticipationDetail> participations = activity.getParticipationDetails();
    List<EFeedback> feedbacks = activity.getFeedbacks();

    // Basic activity details
    ActivityStatisticsVm statistics = ActivityStatisticsVm.builder()
        .activityId(activity.getId())
        .activityName(activity.getActivityName())
        .activityCategory(activity.getActivityCategory().name())
        .activityStatus(activity.getStatus().name())
        .createdDate(activity.getCreatedDate())
        .startDate(activity.getStartDate())
        .endDate(activity.getEndDate())
        .build();

    // Calculate participation statistics
    int totalRegistrations = participations.size();
    int confirmedParticipants = (int) participations.stream()
        .filter(p -> p.getParticipationStatus() == ParticipationStatus.VERIFIED)
        .count();
    int actualAttendees = (int) participations.stream()
        .filter(p -> p.getParticipationStatus() == ParticipationStatus.UNVERIFIED)
        .count();

    double participationRate = totalRegistrations > 0 ? (double) actualAttendees / totalRegistrations * 100 : 0;
    double capacityUtilization = activity.getCapacityLimit() > 0
        ? (double) actualAttendees / activity.getCapacityLimit() * 100
        : 0;

    statistics.setTotalRegistrations(totalRegistrations);
    statistics.setConfirmedParticipants(confirmedParticipants);
    statistics.setActualAttendees(actualAttendees);
    statistics.setParticipationRate(participationRate);
    statistics.setCapacityUtilization(capacityUtilization);

    // Calculate feedback statistics
    if (!feedbacks.isEmpty()) {
      double averageRating = feedbacks.stream()
          .mapToDouble(EFeedback::getRating)
          .average()
          .orElse(0);
      if (averageRating == 0) {
        averageRating = 0.0;
      }
      int highRatingCount = (int) feedbacks.stream()
          .filter(f -> f.getRating() >= 8.0)
          .count();

      int midRatingCount = (int) feedbacks.stream()
          .filter(f -> f.getRating() >= 4.0 && f.getRating() < 8.0)
          .count();

      int lowRatingCount = (int) feedbacks.stream()
          .filter(f -> f.getRating() < 4.0)
          .count();

      statistics.setAverageRating(averageRating);
      statistics.setFeedbackCount(feedbacks.size());
      statistics.setHighRatingCount(highRatingCount);
      statistics.setMidRatingCount(midRatingCount);
      statistics.setLowRatingCount(lowRatingCount);
    }

    // Participant role breakdown
    Map<ParticipationRole, Integer> roleBreakdown = new HashMap<>();
    for (EParticipationDetail participation : participations) {
      ParticipationRole role = participation.getParticipationRole();
      roleBreakdown.put(role, roleBreakdown.getOrDefault(role, 0) + 1);
    }
    statistics.setParticipantsByRole(roleBreakdown);

    // Participation status breakdown
    Map<ParticipationStatus, Integer> statusBreakdown = new HashMap<>();
    for (EParticipationDetail participation : participations) {
      ParticipationStatus status = participation.getParticipationStatus();
      statusBreakdown.put(status, statusBreakdown.getOrDefault(status, 0) + 1);
    }
    statistics.setParticipantsByStatus(statusBreakdown);

    // Timeline statistics
    if (activity.getStartDate() != null && activity.getEndDate() != null) {
      long durationHours = Duration.between(activity.getStartDate(), activity.getEndDate()).toHours();
      statistics.setDurationInHours(durationHours);
    }

    if (activity.getCreatedDate() != null && activity.getStartDate() != null) {
      long daysBeforeStart = Duration.between(activity.getCreatedDate(), activity.getStartDate()).toDays();
      statistics.setDaysBeforeStart(daysBeforeStart);
    }

    // Top participants
    List<ParticipantScoreVm> topParticipants = participations.stream()
        .filter(p -> p.getParticipationStatus() == ParticipationStatus.VERIFIED)
        .map(this::mapToParticipantScoreVm)
        .sorted((p1, p2) -> Double.compare(p2.getScore(), p1.getScore()))
        .limit(10)
        .collect(Collectors.toList());

    statistics.setTopParticipants(topParticipants);

    return statistics;
  }

  @Override
  public ActivityStatisticsVm getActivityStatisticsInTimeRange(Long activityId, Instant startDate, Instant endDate) {
    // Similar to getActivityStatistics but filter participation and feedback data
    // by date range
    ActivityStatisticsVm statistics = getActivityStatistics(activityId);

    Optional<EActivity> activityOpt = activityRepository.findById(activityId);
    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();

    // Filter participations by registration date within the range
    List<EParticipationDetail> filteredParticipations = activity.getParticipationDetails().stream()
        .filter(p -> p.getRegisteredAt() != null &&
            (p.getRegisteredAt().isAfter(startDate) || p.getRegisteredAt().equals(startDate)) &&
            (p.getRegisteredAt().isBefore(endDate) || p.getRegisteredAt().equals(endDate)))
        .collect(Collectors.toList());

    // Recalculate statistics based on filtered data
    int totalRegistrations = filteredParticipations.size();
    int confirmedParticipants = (int) filteredParticipations.stream()
        .filter(p -> p.getParticipationStatus() == ParticipationStatus.VERIFIED)
        .count();
    int actualAttendees = (int) filteredParticipations.stream()
        .filter(p -> p.getParticipationStatus() == ParticipationStatus.UNVERIFIED)
        .count();

    double participationRate = totalRegistrations > 0 ? (double) actualAttendees / totalRegistrations * 100 : 0;

    statistics.setTotalRegistrations(totalRegistrations);
    statistics.setConfirmedParticipants(confirmedParticipants);
    statistics.setActualAttendees(actualAttendees);
    statistics.setParticipationRate(participationRate);

    // Recalculate participant role and status breakdowns
    Map<ParticipationRole, Integer> roleBreakdown = new HashMap<>();
    Map<ParticipationStatus, Integer> statusBreakdown = new HashMap<>();

    for (EParticipationDetail participation : filteredParticipations) {
      ParticipationRole role = participation.getParticipationRole();
      roleBreakdown.put(role, roleBreakdown.getOrDefault(role, 0) + 1);

      ParticipationStatus status = participation.getParticipationStatus();
      statusBreakdown.put(status, statusBreakdown.getOrDefault(status, 0) + 1);
    }

    statistics.setParticipantsByRole(roleBreakdown);
    statistics.setParticipantsByStatus(statusBreakdown);

    return statistics;
  }

  @Override
  public ActivityStatisticsVm getParticipationTrend(Long activityId) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();

    // Get basic statistics as foundation
    ActivityStatisticsVm statistics = getActivityStatistics(activityId);

    // This method could be extended to include more detailed trend data
    // For example, daily registration counts, attendance rates, etc.

    return statistics;
  }

  @Override
  public ActivityStatisticsVm getFeedbackAnalysis(Long activityId) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();
    List<EFeedback> feedbacks = activity.getFeedbacks();

    // Get basic statistics as foundation
    ActivityStatisticsVm statistics = getActivityStatistics(activityId);

    // Additional feedback-specific analytics could be added here
    // For example, sentiment analysis, keyword extraction, etc.

    return statistics;
  }

  @Override
  public ActivityStatisticsVm getParticipantPerformance(Long activityId) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();
    List<EParticipationDetail> participations = activity.getParticipationDetails();

    // Get basic statistics as foundation
    ActivityStatisticsVm statistics = getActivityStatistics(activityId);

    // More detailed participant performance metrics could be added here
    // For example, engagement metrics, assessment scores, etc.

    return statistics;
  }

  private ParticipantScoreVm mapToParticipantScoreVm(EParticipationDetail participation) {
    Double feedbackRating = 0.0;
    String feedbackDescription = "";

    // Get feedback from this participant if available
    if (participation.getFeedbacks() != null && !participation.getFeedbacks().isEmpty()) {
      // Calculate average rating
      feedbackRating = participation.getFeedbacks().stream()
          .mapToDouble(EFeedback::getRating)
          .average()
          .orElse(0);

      // Aggregate feedback descriptions
      feedbackDescription = participation.getFeedbacks().stream()
          .map(EFeedback::getFeedbackDescription)
          .filter(desc -> desc != null && !desc.trim().isEmpty())
          .collect(Collectors.joining(" | "));
    }

    // Assuming a score calculation - this would be replaced with actual score logic
    // For now, we're using a placeholder calculation
    Double score = feedbackRating; // Could be expanded with additional metrics

    return ParticipantScoreVm.builder()
        .feedbackDescription(feedbackDescription)
        .participantId(participation.getParticipant().getId())
        .participantName(participation.getParticipant().getFullName())
        .role(participation.getParticipationRole())
        .score(score)
        .feedbackRating(feedbackRating)
        .build();
  }

  @Override
  public ActivityComparativeAnalysisVm getComparativeAnalysis(Long activityId) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();

    // Calculate category averages for comparison
    Double categoryAverageRating = calculateCategoryAverageRating(activity.getActivityCategory());
    Double categoryAverageParticipationRate = calculateCategoryAverageParticipationRate(activity.getActivityCategory());
    Double categoryAverageCostPerParticipant = calculateCategoryAverageCostPerParticipant(
        activity.getActivityCategory());

    // Get baseline statistics
    ActivityStatisticsVm baseStats = getActivityStatistics(activityId);
    Double activityRating = baseStats.getAverageRating() != null ? baseStats.getAverageRating() : 0.0;
    Double activityParticipationRate = baseStats.getParticipationRate();

    // Calculate comparative metrics
    Double ratingComparison = categoryAverageRating > 0 ? (activityRating / categoryAverageRating) * 100 - 100 : 0;
    Double participationComparison = categoryAverageParticipationRate > 0
        ? (activityParticipationRate / categoryAverageParticipationRate) * 100 - 100
        : 0;

    // Calculate estimated cost per participant (placeholder logic)
    Double costPerParticipant = activity.getFee() != null
        ? activity.getFee() / (activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 1)
        : 0;
    Double costComparison = categoryAverageCostPerParticipant > 0
        ? (costPerParticipant / categoryAverageCostPerParticipant) * 100 - 100
        : 0;

    // Build comparative analysis
    ActivityComparativeAnalysisVm analysis = ActivityComparativeAnalysisVm.builder()
        .activityId(activityId)
        .activityName(activity.getActivityName())
        .averageRatingVsCategoryAverage(ratingComparison)
        .participationRateVsCategoryAverage(participationComparison)
        .costPerParticipantVsCategoryAverage(costComparison)
        .costPerParticipant(costPerParticipant)
        .build();

    // Find similar activities for comparison
    List<SimilarActivityMetricsVm> similarActivities = findSimilarActivities(activityId, 5);
    analysis.setSimilarActivitiesComparison(similarActivities);

    // Find previous runs of this activity if applicable
    List<PreviousRunMetricsVm> previousRuns = findPreviousRuns(activity);
    analysis.setPreviousRunsComparison(previousRuns);

    // Calculate percentile rankings
    analysis.setParticipationPercentile(calculatePercentileRank(activityId, "participation"));
    analysis.setRatingPercentile(calculatePercentileRank(activityId, "rating"));
    analysis.setEngagementPercentile(calculatePercentileRank(activityId, "engagement"));

    return analysis;
  }

  @Override
  public ActivityTimeSeriesVm getTimeSeriesAnalysis(Long activityId) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();
    List<EParticipationDetail> participations = activity.getParticipationDetails();

    // Create time series for registrations (daily counts)
    Map<String, Integer> registrationTimeSeries = new HashMap<>();
    Map<String, List<Integer>> hourlyRegistrations = new HashMap<>(); // For peak time analysis

    // Group registrations by day and hour
    if (participations != null && !participations.isEmpty()) {
      for (EParticipationDetail participation : participations) {
        if (participation.getRegisteredAt() != null) {
          String dateKey = participation.getRegisteredAt().toString().substring(0, 10); // YYYY-MM-DD format
          registrationTimeSeries.put(dateKey, registrationTimeSeries.getOrDefault(dateKey, 0) + 1);

          // Track hourly distribution for peak time analysis
          int hour = participation.getRegisteredAt().atZone(ZoneId.systemDefault()).getHour();
          if (!hourlyRegistrations.containsKey(dateKey)) {
            hourlyRegistrations.put(dateKey, new ArrayList<>(Collections.nCopies(24, 0)));
          }
          List<Integer> hourCounts = hourlyRegistrations.get(dateKey);
          hourCounts.set(hour, hourCounts.get(hour) + 1);
        }
      }
    }

    // Create time series for feedback ratings (if available)
    Map<String, Double> feedbackTimeSeries = new HashMap<>();
    List<EFeedback> allFeedbacks = new ArrayList<>();

    // Collect all feedbacks from participation details
    if (participations != null) {
      for (EParticipationDetail participation : participations) {
        if (participation.getFeedbacks() != null && !participation.getFeedbacks().isEmpty()) {
          allFeedbacks.addAll(participation.getFeedbacks());
        }
      }
    }

    // Group feedbacks by day if they have timestamps
    for (EFeedback feedback : allFeedbacks) {
      if (feedback.getRating() != null) {
        // Use activity's created date as a proxy since feedback doesn't have its own
        // timestamp
        String dateKey = activity.getCreatedDate().toString().substring(0, 10);
        if (!feedbackTimeSeries.containsKey(dateKey)) {
          feedbackTimeSeries.put(dateKey, feedback.getRating().doubleValue());
        } else {
          // Average the ratings for the same day
          double currentAvg = feedbackTimeSeries.get(dateKey);
          int count = registrationTimeSeries.getOrDefault(dateKey, 1); // Use at least 1 to avoid division by zero
          feedbackTimeSeries.put(dateKey, (currentAvg * (count - 1) + feedback.getRating()) / count);
        }
      }
    }

    // Calculate engagement metrics (e.g., participation duration, interaction
    // level)
    Map<String, Double> engagementTimeSeries = calculateEngagementTimeSeries(activity, participations);

    // Calculate social interaction metrics (mentions, shares, etc.)
    Map<String, Integer> socialInteractionTimeSeries = calculateSocialInteractionTimeSeries(activity);

    // Create base time series view model
    ActivityTimeSeriesVm timeSeriesVm = ActivityTimeSeriesVm.builder()
        .activityId(activityId)
        .activityName(activity.getActivityName())
        .registrationTimeSeries(registrationTimeSeries)
        .feedbackTimeSeries(feedbackTimeSeries)
        .engagementTimeSeries(engagementTimeSeries)
        .socialInteractionTimeSeries(socialInteractionTimeSeries)
        .build();

    // Calculate time to capacity (if activity reached capacity)
    if (activity.getCapacityLimit() != null && activity.getCurrentParticipants() != null &&
        activity.getCurrentParticipants() >= activity.getCapacityLimit()) {
      Integer timeToCapacityHours = calculateTimeToCapacity(activity, participations);
      timeSeriesVm.setTimeToCapacityHours(timeToCapacityHours);

      // Calculate percentage compared to average for similar activities
      Double percentOfAverage = calculateTimeToCapacityPercentage(activity, timeToCapacityHours);
      timeSeriesVm.setTimeToCapacityPercentOfAverage(percentOfAverage);
    }

    // Identify peak registration time slots
    List<String> peakRegistrationTimeSlots = identifyPeakRegistrationTimes(hourlyRegistrations);
    timeSeriesVm.setPeakRegistrationTimeSlots(peakRegistrationTimeSlots);

    // Analyze seasonal performance if this is a recurring activity
    List<SeasonalPerformanceVm> seasonalPerformance = analyzeSeasonalPerformance(activity);
    timeSeriesVm.setSeasonalPerformance(seasonalPerformance);

    return timeSeriesVm;
  }

  @Override
  public ActivityComparativeAnalysisVm getEffectivenessMetrics(Long activityId, Double estimatedCost,
      Double estimatedValue) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();
    int participantCount = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;

    // If no cost provided, use activity fee as an estimate (if available)
    Double cost = estimatedCost != null ? estimatedCost
        : (activity.getFee() != null ? activity.getFee() * participantCount : 0);

    // If no value provided, calculate based on attendance score (if available)
    Double value = estimatedValue != null ? estimatedValue
        : (activity.getAttendanceScoreUnit() != null ? activity.getAttendanceScoreUnit() * participantCount
            : cost * 1.5); // Assume 50% ROI by default

    // Calculate effectiveness metrics
    Double roi = cost > 0 ? ((value - cost) / cost) * 100 : 0;
    Double costPerParticipant = participantCount > 0 ? cost / participantCount : 0;
    Double valuePerParticipant = participantCount > 0 ? value / participantCount : 0;

    // Get comparative analysis as baseline
    ActivityComparativeAnalysisVm analysis = getComparativeAnalysis(activityId);

    // Add effectiveness metrics
    analysis.setReturnOnInvestment(roi);
    analysis.setCostPerParticipant(costPerParticipant);
    analysis.setValuePerParticipant(valuePerParticipant);

    return analysis;
  }

  @Override
  public List<SimilarActivityMetricsVm> findSimilarActivities(Long activityId, Integer limit) {
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      throw new RuntimeException("Activity not found with ID: " + activityId);
    }

    EActivity activity = activityOpt.get();

    // Find activities with same category
    List<EActivity> sameCategory = activityRepository.findByActivityCategoryAndIdNot(
        activity.getActivityCategory(), activityId);

    if (sameCategory.isEmpty()) {
      return new ArrayList<>();
    }

    // Calculate similarity score based on various factors
    // This is a simplified implementation
    List<SimilarActivityMetricsVm> similarActivities = new ArrayList<>();

    for (EActivity other : sameCategory) {
      // Calculate basic similarity (higher score = more similar)
      double similarityScore = calculateSimilarityScore(activity, other);

      // Get statistics for comparison
      ActivityStatisticsVm otherStats = getActivityStatistics(other.getId());
      if (otherStats.getAverageRating() == null) {
        otherStats.setAverageRating(0.0);
      }

      ActivityStatisticsVm thisStats = getActivityStatistics(activityId);
      if (thisStats.getAverageRating() == null) {
        thisStats.setAverageRating(0.0);
      }

      // Calculate differences
      int otherParticipantCount = other.getCurrentParticipants() != null ? other.getCurrentParticipants() : 0;
      int thisParticipantCount = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;

      double participantCountDiff = thisParticipantCount > 0
          ? ((double) otherParticipantCount / thisParticipantCount) * 100 - 100
          : 0;

      double participationRateDiff = thisStats.getParticipationRate() > 0
          ? (otherStats.getParticipationRate() / thisStats.getParticipationRate()) * 100 - 100
          : 0;

      double ratingDiff = thisStats.getAverageRating() != null && thisStats.getAverageRating() > 0
          ? ((otherStats.getAverageRating() != null ? otherStats.getAverageRating() : 0.0)
              / thisStats.getAverageRating()) * 100 - 100
          : 0;

      // Create view model
      SimilarActivityMetricsVm similarActivity = SimilarActivityMetricsVm.builder()
          .activityId(other.getId())
          .activityName(other.getActivityName())
          .activityCategory(other.getActivityCategory().name())
          .startDate(other.getStartDate())
          .similarityScore(similarityScore)
          .participantCount(otherParticipantCount)
          .participationRate(otherStats.getParticipationRate())
          .averageRating(otherStats.getAverageRating())
          .participantCountDifference(participantCountDiff)
          .participationRateDifference(participationRateDiff)
          .averageRatingDifference(ratingDiff)
          .build();

      similarActivities.add(similarActivity);
    }

    // Sort by similarity score (highest first) and limit results
    return similarActivities.stream()
        .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
        .limit(limit != null && limit > 0 ? limit : 5)
        .collect(Collectors.toList());
  }

  /**
   * Generates improvement recommendations for an activity based on comparative
   * analysis
   * with similar activities and category averages.
   *
   * @param activityId The ID of the activity to analyze
   * @return ActivityComparativeAnalysisVm containing improvement recommendations
   */
  @Override
  public ActivityComparativeAnalysisVm getImprovementRecommendations(Long activityId) {
    // Get comparative analysis as baseline
    ActivityComparativeAnalysisVm analysis = getComparativeAnalysis(activityId);

    // Find improvement opportunities based on comparative metrics
    Map<String, Double> improvementOpportunities = new HashMap<>();

    // Get similar activities for comparison (limit to 5)
    List<SimilarActivityMetricsVm> similarActivities = findSimilarActivities(activityId, 5);
    analysis.setSimilarActivitiesComparison(similarActivities);

    // Calculate improvement opportunities based on similar activities' performance
    if (similarActivities.isEmpty()) {
      return analysis; // No similar activities found, return basic analysis
    }

    // Extract current activity metrics
    SimilarActivityMetricsVm currentActivity = extractCurrentActivityMetrics(activityId, similarActivities);
    if (currentActivity == null) {
      return analysis; // Cannot find current activity in the similar activities list
    }

    // Calculate average metrics from similar activities (excluding current
    // activity)
    double avgRating = calculateAverageRating(similarActivities, activityId);
    double avgParticipationRate = calculateAverageParticipationRate(similarActivities, activityId);
    double avgCostPerParticipant = calculateAverageCostPerParticipant(similarActivities, activityId);

    // Extract current activity's metrics
    double currentRating = currentActivity.getAverageRating();
    double currentParticipationRate = currentActivity.getParticipationRate();
    double currentCostPerParticipant = currentActivity.getCostPerParticipant();

    // Add category-based improvement opportunities
    addCategoryBasedImprovements(analysis, improvementOpportunities);

    // Add similar activities-based improvement opportunities
    addSimilarActivitiesImprovements(
        currentRating, avgRating,
        currentParticipationRate, avgParticipationRate,
        currentCostPerParticipant, avgCostPerParticipant,
        improvementOpportunities);

    // Add recommendations from top performers
    addTopPerformerRecommendations(similarActivities, improvementOpportunities);

    // Add improvement opportunities to analysis
    analysis.setImprovementOpportunities(improvementOpportunities);

    return analysis;
  }

  /**
   * Extracts the current activity from the similar activities list
   */
  private SimilarActivityMetricsVm extractCurrentActivityMetrics(Long activityId,
      List<SimilarActivityMetricsVm> similarActivities) {
    return similarActivities.stream()
        .filter(a -> a.getActivityId().equals(activityId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Calculates the average rating from similar activities, excluding the current
   * activity
   */
  private double calculateAverageRating(List<SimilarActivityMetricsVm> similarActivities, Long currentActivityId) {
    return similarActivities.stream()
        .filter(a -> !a.getActivityId().equals(currentActivityId))
        .mapToDouble(SimilarActivityMetricsVm::getAverageRating)
        .average()
        .orElse(0.0);
  }

  /**
   * Calculates the average participation rate from similar activities, excluding
   * the current activity
   */
  private double calculateAverageParticipationRate(List<SimilarActivityMetricsVm> similarActivities,
      Long currentActivityId) {
    return similarActivities.stream()
        .filter(a -> !a.getActivityId().equals(currentActivityId))
        .mapToDouble(SimilarActivityMetricsVm::getParticipationRate)
        .average()
        .orElse(0.0);
  }

  /**
   * Calculates the average cost per participant from similar activities,
   * excluding the current activity
   */
  private double calculateAverageCostPerParticipant(List<SimilarActivityMetricsVm> similarActivities,
      Long currentActivityId) {
    return similarActivities.stream()
        .filter(a -> !a.getActivityId().equals(currentActivityId))
        .mapToDouble(SimilarActivityMetricsVm::getCostPerParticipant)
        .average()
        .orElse(0.0);
  }

  /**
   * Adds improvement opportunities based on category averages
   */
  private void addCategoryBasedImprovements(ActivityComparativeAnalysisVm analysis,
      Map<String, Double> improvementOpportunities) {
    if (analysis.getAverageRatingVsCategoryAverage() != null && analysis.getAverageRatingVsCategoryAverage() < 0) {
      double ratingGap = Math.abs(analysis.getAverageRatingVsCategoryAverage());
      improvementOpportunities.put("Participant Satisfaction (vs Category)", ratingGap);
    }

    if (analysis.getParticipationRateVsCategoryAverage() != null
        && analysis.getParticipationRateVsCategoryAverage() < 0) {
      double participationGap = Math.abs(analysis.getParticipationRateVsCategoryAverage());
      improvementOpportunities.put("Attendance Rate (vs Category)", participationGap);
    }

    if (analysis.getCostPerParticipantVsCategoryAverage() != null
        && analysis.getCostPerParticipantVsCategoryAverage() > 0) {
      double costEfficiencyGap = analysis.getCostPerParticipantVsCategoryAverage();
      improvementOpportunities.put("Cost Efficiency (vs Category)", costEfficiencyGap);
    }
  }

  /**
   * Adds improvement opportunities based on similar activities' averages
   */
  private void addSimilarActivitiesImprovements(
      double currentRating, double avgRating,
      double currentParticipationRate, double avgParticipationRate,
      double currentCostPerParticipant, double avgCostPerParticipant,
      Map<String, Double> improvementOpportunities) {

    // Only add if there's a meaningful difference (current < average for positive
    // metrics)
    if (currentRating < avgRating) {
      double ratingGapVsSimilar = avgRating - currentRating;
      improvementOpportunities.put("Participant Satisfaction (vs Similar)", ratingGapVsSimilar);
    }

    if (currentParticipationRate < avgParticipationRate) {
      double participationGapVsSimilar = avgParticipationRate - currentParticipationRate;
      improvementOpportunities.put("Attendance Rate (vs Similar)", participationGapVsSimilar);
    }

    // For cost, lower is better (current > average indicates improvement needed)
    if (currentCostPerParticipant > avgCostPerParticipant) {
      double costGapVsSimilar = currentCostPerParticipant - avgCostPerParticipant;
      improvementOpportunities.put("Cost Efficiency (vs Similar)", costGapVsSimilar);
    }
  }

  /**
   * Adds recommendations based on top performing similar activities
   */
  private void addTopPerformerRecommendations(List<SimilarActivityMetricsVm> similarActivities,
      Map<String, Double> improvementOpportunities) {
    List<SimilarActivityMetricsVm> topPerformers = similarActivities.stream()
        .filter(a -> a.getAverageRatingDifference() != null && a.getAverageRatingDifference() > 0)
        .sorted((a, b) -> Double.compare(b.getAverageRatingDifference(), a.getAverageRatingDifference()))
        .limit(3)
        .collect(Collectors.toList());

    if (!topPerformers.isEmpty()) {
      double successScore = topPerformers.stream()
          .mapToDouble(SimilarActivityMetricsVm::getAverageRatingDifference)
          .average()
          .orElse(0.0);

      improvementOpportunities.put("Learn from Top Performers", successScore);
    }
  }

  private Double calculateTimeToCapacityPercentage(EActivity activity, Integer timeToCapacityHours) {
    if (timeToCapacityHours == null) {
      return null;
    }

    // Get all activities in the same category
    List<EActivity> similarActivities = activityRepository.findByActivityCategoryAndIdNot(
        activity.getActivityCategory(), activity.getId());

    if (similarActivities.isEmpty()) {
      return 100.0; // No comparison data available
    }

    // Calculate average time to capacity for similar activities
    double totalTimeToCapacity = 0.0;
    int activitiesWithCapacityData = 0;

    for (EActivity similarActivity : similarActivities) {
      if (similarActivity.getCapacityLimit() != null && similarActivity.getCurrentParticipants() != null &&
          similarActivity.getCurrentParticipants() >= similarActivity.getCapacityLimit()) {

        List<EParticipationDetail> participations = similarActivity.getParticipationDetails();
        if (participations != null && !participations.isEmpty()) {
          // Sort participations by registration time
          List<EParticipationDetail> sortedParticipations = participations.stream()
              .filter(p -> p.getRegisteredAt() != null)
              .sorted(Comparator.comparing(EParticipationDetail::getRegisteredAt))
              .collect(Collectors.toList());

          if (sortedParticipations.size() >= similarActivity.getCapacityLimit()) {
            // Calculate time to capacity for this activity
            EParticipationDetail firstRegistration = sortedParticipations.get(0);
            EParticipationDetail capacityRegistration = sortedParticipations
                .get(similarActivity.getCapacityLimit() - 1);

            long hoursToCapacity = ChronoUnit.HOURS.between(
                firstRegistration.getRegisteredAt(),
                capacityRegistration.getRegisteredAt());

            totalTimeToCapacity += hoursToCapacity;
            activitiesWithCapacityData++;
          }
        }
      }
    }

    if (activitiesWithCapacityData == 0) {
      return 100.0; // No comparison data available
    }

    double averageTimeToCapacity = totalTimeToCapacity / activitiesWithCapacityData;

    // Calculate percentage (lower is better - filled faster than average)
    return (timeToCapacityHours / averageTimeToCapacity) * 100.0;
  }

  // Helper methods for new functionality

  private Double calculateCategoryAverageRating(ActivityCategory category) {
    // Use the repository method to get the average rating for the category
    Double averageRating = feedbackRepository.getAverageRatingByCategory(category);

    // If no ratings are available, return a default value
    if (averageRating == null) {
      // Calculate the overall average rating as a fallback
      averageRating = feedbackRepository.getAverageRating();

      // If still null, provide a reasonable default
      if (averageRating == null) {
        return 0.0;
      }
    }

    return averageRating;
  }

  private Double calculateCategoryAverageParticipationRate(ActivityCategory category) {
    // Get all activities in this category
    Long totalActivitiesInCategory = activityRepository.countActivitiesByCategory(category);

    if (totalActivitiesInCategory == null || totalActivitiesInCategory == 0) {
      return 0.0;
    }

    // Calculate the sum of participation rates for all activities in this category
    // Participation rate = (currentParticipants / capacityLimit) * 100

    // Get all activities in this category by using the existing repository method
    // and filtering out any activities that don't match the category
    List<EActivity> allActivities = activityRepository.findAll();
    List<EActivity> activitiesInCategory = allActivities.stream()
        .filter(activity -> activity.getActivityCategory() == category)
        .collect(Collectors.toList());

    if (activitiesInCategory == null || activitiesInCategory.isEmpty()) {
      return 0.0;
    }

    double totalParticipationRate = 0.0;
    int activitiesWithCapacity = 0;

    for (EActivity activity : activitiesInCategory) {
      if (activity.getCapacityLimit() != null && activity.getCapacityLimit() > 0 &&
          activity.getCurrentParticipants() != null) {
        double participationRate = ((double) activity.getCurrentParticipants() / activity.getCapacityLimit()) * 100.0;
        totalParticipationRate += participationRate;
        activitiesWithCapacity++;
      }
    }

    // Calculate the average participation rate
    if (activitiesWithCapacity > 0) {
      return totalParticipationRate / activitiesWithCapacity;
    } else {
      return 0.0;
    }
  }

  private Double calculateCategoryAverageCostPerParticipant(ActivityCategory category) {
    // Get all activities in this category by using the existing repository method
    // and filtering out any activities that don't match the category
    List<EActivity> allActivities = activityRepository.findAll();
    List<EActivity> activitiesInCategory = allActivities.stream()
        .filter(activity -> activity.getActivityCategory() == category)
        .collect(Collectors.toList());

    if (activitiesInCategory == null || activitiesInCategory.isEmpty()) {
      return 0.0;
    }

    double totalCost = 0.0;
    int totalParticipants = 0;
    int activitiesWithFeeAndParticipants = 0;

    for (EActivity activity : activitiesInCategory) {
      if (activity.getFee() != null && activity.getFee() > 0 &&
          activity.getCurrentParticipants() != null && activity.getCurrentParticipants() > 0) {
        totalCost += activity.getFee() * activity.getCurrentParticipants();
        totalParticipants += activity.getCurrentParticipants();
        activitiesWithFeeAndParticipants++;
      }
    }

    // Calculate the average cost per participant
    if (totalParticipants > 0) {
      return totalCost / totalParticipants;
    } else {
      return 0.0;
    }
  }

  private Integer calculatePercentileRank(Long activityId, String metric) {
    // Get the activity
    Optional<EActivity> activityOpt = activityRepository.findById(activityId);

    if (!activityOpt.isPresent()) {
      return 0;
    }

    EActivity targetActivity = activityOpt.get();
    ActivityCategory category = targetActivity.getActivityCategory();

    // Get all activities in the same category for comparison
    List<EActivity> similarActivities = activityRepository.findByActivityCategoryAndIdNot(category, activityId);

    if (similarActivities == null || similarActivities.isEmpty()) {
      return 100; // If there are no other activities to compare with, this activity is in the
                  // 100th percentile
    }

    // Calculate percentile based on the specified metric
    switch (metric) {
      case "participation_rate":
        return calculateParticipationRatePercentile(targetActivity, similarActivities);
      case "rating":
        return calculateRatingPercentile(targetActivity, similarActivities);
      case "cost_efficiency":
        return calculateCostEfficiencyPercentile(targetActivity, similarActivities);
      default:
        return 50; // Default middle percentile if metric is not recognized
    }
  }

  private Integer calculateParticipationRatePercentile(EActivity targetActivity, List<EActivity> similarActivities) {
    if (targetActivity.getCapacityLimit() == null || targetActivity.getCapacityLimit() == 0 ||
        targetActivity.getCurrentParticipants() == null) {
      return 0;
    }

    // Calculate participation rate for the target activity
    double targetParticipationRate = ((double) targetActivity.getCurrentParticipants()
        / targetActivity.getCapacityLimit()) * 100.0;

    // Count how many activities have a lower participation rate
    int countLower = 0;

    for (EActivity activity : similarActivities) {
      if (activity.getCapacityLimit() != null && activity.getCapacityLimit() > 0 &&
          activity.getCurrentParticipants() != null) {
        double participationRate = ((double) activity.getCurrentParticipants() / activity.getCapacityLimit()) * 100.0;

        if (participationRate < targetParticipationRate) {
          countLower++;
        }
      }
    }

    // Calculate percentile
    return (int) Math.round((double) countLower / similarActivities.size() * 100);
  }

  private Integer calculateRatingPercentile(EActivity targetActivity, List<EActivity> similarActivities) {
    // Get average rating for the target activity
    Double targetRating = feedbackRepository.getAverageRatingForActivity(targetActivity.getId());

    if (targetRating == null) {
      return 0;
    }

    // Count how many activities have a lower rating
    int countLower = 0;

    for (EActivity activity : similarActivities) {
      Double rating = feedbackRepository.getAverageRatingForActivity(activity.getId());

      if (rating != null && rating < targetRating) {
        countLower++;
      }
    }

    // Calculate percentile
    return (int) Math.round((double) countLower / similarActivities.size() * 100);
  }

  private Integer calculateCostEfficiencyPercentile(EActivity targetActivity, List<EActivity> similarActivities) {
    if (targetActivity.getFee() == null || targetActivity.getFee() == 0 ||
        targetActivity.getCurrentParticipants() == null || targetActivity.getCurrentParticipants() == 0) {
      return 0;
    }

    // Calculate cost per participant for the target activity
    double targetCostPerParticipant = targetActivity.getFee() / targetActivity.getCurrentParticipants();

    // For cost efficiency, lower is better, so we count activities with higher cost
    // per participant
    int countHigher = 0;

    for (EActivity activity : similarActivities) {
      if (activity.getFee() != null && activity.getFee() > 0 &&
          activity.getCurrentParticipants() != null && activity.getCurrentParticipants() > 0) {
        double costPerParticipant = activity.getFee() / activity.getCurrentParticipants();

        if (costPerParticipant > targetCostPerParticipant) {
          countHigher++;
        }
      }
    }

    // Calculate percentile
    return (int) Math.round((double) countHigher / similarActivities.size() * 100);
  }

  private List<PreviousRunMetricsVm> findPreviousRuns(EActivity activity) {
    // This would find previous instances of the same or similar activities
    // Placeholder implementation
    return new ArrayList<>(); // Empty list for now
  }

  private List<SeasonalPerformanceVm> analyzeSeasonalPerformance(EActivity activity) {
    // This would analyze seasonal patterns for this activity type
    // Placeholder implementation - in a real system, we would analyze historical
    // data
    // for similar activities across different seasons/months
    List<SeasonalPerformanceVm> seasonalData = new ArrayList<>();

    // Example: Add some sample seasonal data based on activity category
    if (activity.getActivityCategory() != null) {
      String category = activity.getActivityCategory().name();

      // Example data for demonstration purposes
      if ("Seminar".equalsIgnoreCase(category)) {
        seasonalData.add(SeasonalPerformanceVm.builder().season("Spring").participationRate(85.0).averageRating(4.5)
            .participantCount(120).engagementScore(0.85).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Summer").participationRate(65.0).averageRating(4.2)
            .participantCount(95).engagementScore(0.75).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Fall").participationRate(90.0).averageRating(4.7)
            .participantCount(150).engagementScore(0.92).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Winter").participationRate(75.0).averageRating(4.3)
            .participantCount(110).engagementScore(0.80).build());
      } else if ("Workshop".equalsIgnoreCase(category)) {
        seasonalData.add(SeasonalPerformanceVm.builder().season("Spring").participationRate(80.0).averageRating(4.4)
            .participantCount(115).engagementScore(0.82).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Summer").participationRate(85.0).averageRating(4.5)
            .participantCount(125).engagementScore(0.87).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Fall").participationRate(82.0).averageRating(4.6)
            .participantCount(120).engagementScore(0.85).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Winter").participationRate(70.0).averageRating(4.2)
            .participantCount(100).engagementScore(0.78).build());
      } else if ("Volunteer".equalsIgnoreCase(category)) {
        seasonalData.add(SeasonalPerformanceVm.builder().season("Spring").participationRate(90.0).averageRating(4.8)
            .participantCount(160).engagementScore(0.95).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Summer").participationRate(95.0).averageRating(4.9)
            .participantCount(180).engagementScore(0.97).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Fall").participationRate(85.0).averageRating(4.7)
            .participantCount(140).engagementScore(0.88).build());
        seasonalData.add(SeasonalPerformanceVm.builder().season("Winter").participationRate(60.0).averageRating(4.0)
            .participantCount(85).engagementScore(0.70).build());
      }
    }

    return seasonalData;
  }

  /**
   * Calculates engagement time series data for an activity
   * 
   * @param activity       The activity to analyze
   * @param participations List of participation details
   * @return Map of dates to engagement scores
   */
  private Map<String, Double> calculateEngagementTimeSeries(EActivity activity,
      List<EParticipationDetail> participations) {
    Map<String, Double> engagementTimeSeries = new HashMap<>();

    // In a real implementation, this would analyze actual engagement metrics
    // such as time spent in activity, interaction level, etc.
    // For this example, we'll create synthetic engagement data based on the
    // activity date

    if (activity.getStartDate() != null) {
      // Generate engagement data for a week starting from the activity start date
      String startDateStr = activity.getStartDate().toString().substring(0, 10);

      // Base engagement score (could be derived from actual metrics in a real system)
      double baseEngagementScore = 0.7;

      // Add the start date with a medium engagement score
      engagementTimeSeries.put(startDateStr, baseEngagementScore);

      // If multi-day activity, add more engagement data with a curve
      // (typically higher in the middle, lower at the beginning and end)
      if (activity.getEndDate() != null &&
          !activity.getStartDate().equals(activity.getEndDate())) {

        long daysBetween = ChronoUnit.DAYS.between(
            activity.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate(),
            activity.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate());

        // Generate data for each day of the activity
        for (int i = 1; i <= daysBetween; i++) {
          // Create a curve with peak in the middle
          double progress = (double) i / (daysBetween + 1);
          double engagementFactor = 1.0 - 2.0 * Math.abs(progress - 0.5);
          double dailyEngagement = baseEngagementScore + (0.3 * engagementFactor);

          // Format date as YYYY-MM-DD
          String dateKey = activity.getStartDate().plus(i, ChronoUnit.DAYS)
              .atZone(ZoneId.systemDefault()).toLocalDate().toString();

          engagementTimeSeries.put(dateKey, dailyEngagement);
        }
      }
    }

    return engagementTimeSeries;
  }

  /**
   * Calculates social interaction metrics for an activity
   * 
   * @param activity The activity to analyze
   * @return Map of dates to social interaction counts
   */
  private Map<String, Integer> calculateSocialInteractionTimeSeries(EActivity activity) {
    Map<String, Integer> socialInteractionTimeSeries = new HashMap<>();

    // In a real implementation, this would pull data from social media APIs
    // or other sources tracking mentions, shares, etc.
    // For this example, we'll create synthetic social interaction data

    if (activity.getStartDate() != null) {
      // Generate data for a week before and after the activity
      String startDateStr = activity.getStartDate().toString().substring(0, 10);

      // Simulate social media activity pattern:
      // builds up before the event, peaks during, then gradually declines

      // Pre-event buzz (3 days before)
      for (int i = -3; i < 0; i++) {
        String dateKey = activity.getStartDate().plus(i, ChronoUnit.DAYS)
            .atZone(ZoneId.systemDefault()).toLocalDate().toString();

        // Increasing social mentions as event approaches
        int mentions = 5 + (5 * Math.abs(i));
        socialInteractionTimeSeries.put(dateKey, mentions);
      }

      // Event day - peak social activity
      socialInteractionTimeSeries.put(startDateStr, 25);

      // Post-event discussion (4 days after)
      for (int i = 1; i <= 4; i++) {
        String dateKey = activity.getStartDate().plus(i, ChronoUnit.DAYS)
            .atZone(ZoneId.systemDefault()).toLocalDate().toString();

        // Decreasing social mentions after event
        int mentions = 20 - (i * 4);
        socialInteractionTimeSeries.put(dateKey, mentions);
      }
    }

    return socialInteractionTimeSeries;
  }

  /**
   * Calculates the time to capacity for an activity in hours
   * 
   * @param activity       The activity to analyze
   * @param participations List of participation details
   * @return Time to capacity in hours, or null if not applicable
   */
  private Integer calculateTimeToCapacity(EActivity activity, List<EParticipationDetail> participations) {
    // If activity has no capacity limit or hasn't reached capacity, return null
    if (activity.getCapacityLimit() == null ||
        activity.getCurrentParticipants() == null ||
        activity.getCurrentParticipants() < activity.getCapacityLimit() ||
        participations == null || participations.isEmpty()) {
      return null;
    }

    // Sort participations by registration time
    List<EParticipationDetail> sortedParticipations = participations.stream()
        .filter(p -> p.getRegisteredAt() != null)
        .sorted(Comparator.comparing(EParticipationDetail::getRegisteredAt))
        .collect(Collectors.toList());

    if (sortedParticipations.size() < activity.getCapacityLimit()) {
      return null; // Not enough data to determine time to capacity
    }

    // Get the first and capacity-limit-th registration times
    EParticipationDetail firstRegistration = sortedParticipations.get(0);
    EParticipationDetail capacityRegistration = sortedParticipations.get(activity.getCapacityLimit() - 1);

    // Calculate hours between first registration and capacity being reached
    long hoursToCapacity = ChronoUnit.HOURS.between(
        firstRegistration.getRegisteredAt(),
        capacityRegistration.getRegisteredAt());

    return (int) hoursToCapacity;
  }

  /**
   * Identifies peak registration time slots based on hourly registration data
   * 
   * @param hourlyRegistrations Map of dates to hourly registration counts
   * @return List of peak registration time slots (e.g., "Monday 18:00-19:00")
   */
  private List<String> identifyPeakRegistrationTimes(Map<String, List<Integer>> hourlyRegistrations) {
    List<String> peakTimeSlots = new ArrayList<>();

    if (hourlyRegistrations == null || hourlyRegistrations.isEmpty()) {
      return peakTimeSlots;
    }

    // Aggregate hourly data across all days to find overall patterns
    int[] aggregatedHourlyData = new int[24];

    for (List<Integer> dailyData : hourlyRegistrations.values()) {
      for (int hour = 0; hour < 24 && hour < dailyData.size(); hour++) {
        aggregatedHourlyData[hour] += dailyData.get(hour);
      }
    }

    // Find the top 3 peak hours
    List<Integer> topHours = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      int maxHour = 0;
      int maxCount = -1;

      for (int hour = 0; hour < 24; hour++) {
        if (aggregatedHourlyData[hour] > maxCount && !topHours.contains(hour)) {
          maxCount = aggregatedHourlyData[hour];
          maxHour = hour;
        }
      }

      if (maxCount > 0) {
        topHours.add(maxHour);
      }
    }

    // Convert hours to time slot strings
    for (int hour : topHours) {
      String startTime = String.format("%02d:00", hour);
      String endTime = String.format("%02d:00", (hour + 1) % 24);
      peakTimeSlots.add(startTime + "-" + endTime);
    }

    return peakTimeSlots;
  }

  private double calculateSimilarityScore(EActivity activity1, EActivity activity2) {
    // Simplified similarity calculation based on available attributes
    double score = 0.0;

    // Same category is already a filter, but we'll include it for completeness
    if (activity1.getActivityCategory() == activity2.getActivityCategory()) {
      score += 30.0;
    }

    // Similar capacity
    if (activity1.getCapacityLimit() != null && activity2.getCapacityLimit() != null) {
      int capacityDiff = Math.abs(activity1.getCapacityLimit() - activity2.getCapacityLimit());
      double capacitySimilarity = capacityDiff < 10 ? (10 - capacityDiff) / 10.0 * 20.0 : 0;
      score += capacitySimilarity;
    }

    // Similar fee structure
    if (activity1.getFee() != null && activity2.getFee() != null) {
      double feeDiff = Math.abs(activity1.getFee() - activity2.getFee());
      double feeSimilarity = feeDiff < 50 ? (50 - feeDiff) / 50.0 * 20.0 : 0;
      score += feeSimilarity;
    }

    // Similar tags (if available)
    if (activity1.getTags() != null && activity2.getTags() != null && !activity1.getTags().isEmpty()
        && !activity2.getTags().isEmpty()) {
      Set<String> tags1 = new HashSet<>(activity1.getTags());
      Set<String> tags2 = new HashSet<>(activity2.getTags());

      Set<String> intersection = new HashSet<>(tags1);
      intersection.retainAll(tags2);

      Set<String> union = new HashSet<>(tags1);
      union.addAll(tags2);

      double jaccardSimilarity = (double) intersection.size() / union.size();
      score += jaccardSimilarity * 30.0;
    }

    return score;
  }
}
