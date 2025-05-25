package com.winnguyen1905.Activity.rest.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.model.viewmodel.ActivityComparativeAnalysisVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityStatisticsVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityTimeSeriesVm;
import com.winnguyen1905.Activity.model.viewmodel.ParticipantScoreVm;
import com.winnguyen1905.Activity.model.viewmodel.PreviousRunMetricsVm;
import com.winnguyen1905.Activity.model.viewmodel.SeasonalPerformanceVm;
import com.winnguyen1905.Activity.model.viewmodel.SimilarActivityMetricsVm;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EFeedback;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.Activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.Activity.rest.service.ActivityStatisticsService;

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
                .filter(p -> p.getParticipationStatus() == ParticipationStatus.UNVERIFIED)
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

        // Get feedback from this participant if available
        if (participation.getFeedbacks() != null && !participation.getFeedbacks().isEmpty()) {
            feedbackRating = participation.getFeedbacks().stream()
                    .mapToDouble(EFeedback::getRating)
                    .average()
                    .orElse(0);
        }

        // Assuming a score calculation - this would be replaced with actual score logic
        // For now, we're using a placeholder calculation
        Double score = feedbackRating; // Could be expanded with additional metrics

        return ParticipantScoreVm.builder()
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
        Double categoryAverageCostPerParticipant = calculateCategoryAverageCostPerParticipant(activity.getActivityCategory());
        
        // Get baseline statistics
        ActivityStatisticsVm baseStats = getActivityStatistics(activityId);
        Double activityRating = baseStats.getAverageRating() != null ? baseStats.getAverageRating() : 0.0;
        Double activityParticipationRate = baseStats.getParticipationRate();
        
        // Calculate comparative metrics
        Double ratingComparison = categoryAverageRating > 0 ? (activityRating / categoryAverageRating) * 100 - 100 : 0;
        Double participationComparison = categoryAverageParticipationRate > 0 ? 
                (activityParticipationRate / categoryAverageParticipationRate) * 100 - 100 : 0;
        
        // Calculate estimated cost per participant (placeholder logic)
        Double costPerParticipant = activity.getFee() != null ? 
                activity.getFee() / (activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 1) : 0;
        Double costComparison = categoryAverageCostPerParticipant > 0 ? 
                (costPerParticipant / categoryAverageCostPerParticipant) * 100 - 100 : 0;
        
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
        
        // Group registrations by day
        if (participations != null && !participations.isEmpty()) {
            for (EParticipationDetail participation : participations) {
                if (participation.getRegisteredAt() != null) {
                    String dateKey = participation.getRegisteredAt().toString().substring(0, 10); // YYYY-MM-DD format
                    registrationTimeSeries.put(dateKey, registrationTimeSeries.getOrDefault(dateKey, 0) + 1);
                }
            }
        }
        
        // Create time series for feedback ratings (if available)
        Map<String, Double> feedbackTimeSeries = new HashMap<>();
        // Get feedback data for potential future implementation
        // List<EFeedback> feedbacks = activity.getFeedbacks();
        
        // This would need timestamp data on feedback which might not be available
        // Placeholder implementation assuming feedback has timestamp
        
        // Create base time series view model
        ActivityTimeSeriesVm timeSeriesVm = ActivityTimeSeriesVm.builder()
                .activityId(activityId)
                .activityName(activity.getActivityName())
                .registrationTimeSeries(registrationTimeSeries)
                .feedbackTimeSeries(feedbackTimeSeries)
                .build();
        
        // Calculate time to capacity (if activity reached capacity)
        if (activity.getCapacityLimit() != null && activity.getCurrentParticipants() != null && 
                activity.getCurrentParticipants() >= activity.getCapacityLimit()) {
            // This would require more detailed data about registration timestamps
            // Placeholder implementation
            timeSeriesVm.setTimeToCapacityHours(48); // Example value
        }
        
        // Analyze seasonal performance if this is a recurring activity
        List<SeasonalPerformanceVm> seasonalPerformance = analyzeSeasonalPerformance(activity);
        timeSeriesVm.setSeasonalPerformance(seasonalPerformance);
        
        return timeSeriesVm;
    }
    
    @Override
    public ActivityComparativeAnalysisVm getEffectivenessMetrics(Long activityId, Double estimatedCost, Double estimatedValue) {
        Optional<EActivity> activityOpt = activityRepository.findById(activityId);
        
        if (!activityOpt.isPresent()) {
            throw new RuntimeException("Activity not found with ID: " + activityId);
        }
        
        EActivity activity = activityOpt.get();
        int participantCount = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;
        
        // If no cost provided, use activity fee as an estimate (if available)
        Double cost = estimatedCost != null ? estimatedCost : 
                     (activity.getFee() != null ? activity.getFee() * participantCount : 0);
        
        // If no value provided, calculate based on attendance score (if available)
        Double value = estimatedValue != null ? estimatedValue : 
                      (activity.getAttendanceScoreUnit() != null ? 
                       activity.getAttendanceScoreUnit() * participantCount : cost * 1.5); // Assume 50% ROI by default
        
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
            ActivityStatisticsVm thisStats = getActivityStatistics(activityId);
            
            // Calculate differences
            int otherParticipantCount = other.getCurrentParticipants() != null ? other.getCurrentParticipants() : 0;
            int thisParticipantCount = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;
            
            double participantCountDiff = thisParticipantCount > 0 ? 
                    ((double)otherParticipantCount / thisParticipantCount) * 100 - 100 : 0;
                    
            double participationRateDiff = thisStats.getParticipationRate() > 0 ? 
                    (otherStats.getParticipationRate() / thisStats.getParticipationRate()) * 100 - 100 : 0;
                    
            double ratingDiff = thisStats.getAverageRating() != null && thisStats.getAverageRating() > 0 ? 
                    (otherStats.getAverageRating() / thisStats.getAverageRating()) * 100 - 100 : 0;
            
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
    
    @Override
    public ActivityComparativeAnalysisVm getImprovementRecommendations(Long activityId) {
        // Get comparative analysis as baseline
        ActivityComparativeAnalysisVm analysis = getComparativeAnalysis(activityId);
        
        // Find improvement opportunities based on comparative metrics
        Map<String, Double> improvementOpportunities = new HashMap<>();
        
        // Identify areas where this activity underperforms compared to category averages
        if (analysis.getAverageRatingVsCategoryAverage() != null && analysis.getAverageRatingVsCategoryAverage() < 0) {
            // Negative value means below average
            improvementOpportunities.put("Participant Satisfaction", Math.abs(analysis.getAverageRatingVsCategoryAverage()));
        }
        
        if (analysis.getParticipationRateVsCategoryAverage() != null && analysis.getParticipationRateVsCategoryAverage() < 0) {
            improvementOpportunities.put("Attendance Rate", Math.abs(analysis.getParticipationRateVsCategoryAverage()));
        }
        
        if (analysis.getCostPerParticipantVsCategoryAverage() != null && analysis.getCostPerParticipantVsCategoryAverage() > 0) {
            // Positive value means above average cost (less efficient)
            improvementOpportunities.put("Cost Efficiency", analysis.getCostPerParticipantVsCategoryAverage());
        }
        
        // Add more improvement opportunities based on similar successful activities
        if (analysis.getSimilarActivitiesComparison() != null && !analysis.getSimilarActivitiesComparison().isEmpty()) {
            // Find top performing similar activities
            List<SimilarActivityMetricsVm> topPerformers = analysis.getSimilarActivitiesComparison().stream()
                    .filter(a -> a.getAverageRatingDifference() != null && a.getAverageRatingDifference() > 0)
                    .sorted((a, b) -> Double.compare(b.getAverageRatingDifference(), a.getAverageRatingDifference()))
                    .limit(3)
                    .collect(Collectors.toList());
                    
            if (!topPerformers.isEmpty()) {
                // Extract success factors from top performers (this would need actual success factor data)
                improvementOpportunities.put("Learn from Similar Activities", 50.0); // Placeholder score
            }
        }
        
        // Add improvement opportunities to analysis
        analysis.setImprovementOpportunities(improvementOpportunities);
        
        return analysis;
    }
    
    // Helper methods for new functionality
    
    private Double calculateCategoryAverageRating(ActivityCategory category) {
        // This would be implemented with actual repository query
        // Placeholder implementation
        return 7.5; // Example value
    }
    
    private Double calculateCategoryAverageParticipationRate(ActivityCategory category) {
        // This would be implemented with actual repository query
        // Placeholder implementation
        return 75.0; // Example value
    }
    
    private Double calculateCategoryAverageCostPerParticipant(ActivityCategory category) {
        // This would be implemented with actual repository query
        // Placeholder implementation
        return 50.0; // Example value
    }
    
    private Integer calculatePercentileRank(Long activityId, String metric) {
        // This would be implemented with actual percentile calculation
        // Placeholder implementation
        return 65; // Example value
    }
    
    private List<PreviousRunMetricsVm> findPreviousRuns(EActivity activity) {
        // This would find previous instances of the same or similar activities
        // Placeholder implementation
        return new ArrayList<>(); // Empty list for now
    }
    
    private List<SeasonalPerformanceVm> analyzeSeasonalPerformance(EActivity activity) {
        // This would analyze seasonal patterns for this activity type
        // Placeholder implementation
        return new ArrayList<>(); // Empty list for now
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
        if (activity1.getTags() != null && activity2.getTags() != null && !activity1.getTags().isEmpty() && !activity2.getTags().isEmpty()) {
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
