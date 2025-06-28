package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.common.constant.TimePeriod;
import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.KeywordCountVm;
import com.winnguyen1905.activity.model.viewmodel.StatisticsVm;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.activity.rest.service.StatisticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

  // List of common words to exclude from keyword analysis
  private static final List<String> COMMON_WORDS = List.of(
      "this", "that", "these", "those", "with", "from", "have", "has", "had",
      "what", "when", "where", "which", "who", "whom", "whose", "why", "how",
      "there", "here", "were", "their", "they", "them", "then", "than",
      "your", "you", "our", "ours", "about", "would", "could", "should",
      "will", "shall", "may", "might", "must", "can", "such", "like", "just");

  private final ActivityRepository activityRepository;
  private final FeedbackRepository feedbackRepository;

  private static final int MAX_KEYWORDS = 10;

  /**
   * Checks if a word is a common word that should be excluded from keyword
   * analysis
   * 
   * @param word The word to check
   * @return true if the word is common and should be excluded
   */
  private boolean isCommonWord(String word) {
    return COMMON_WORDS.contains(word.toLowerCase());
  }

  /**
   * Calculate start and end dates based on time period
   * 
   * @param timePeriod The time period to calculate
   * @return An array of Instant objects [startDate, endDate]
   */
  private Instant[] calculateDateRange(TimePeriod timePeriod) {
    Instant now = Instant.now();
    Instant startDate;

    switch (timePeriod) {
      case DAY:
        startDate = now.minus(1, ChronoUnit.DAYS);
        break;
      case WEEK:
        startDate = now.minus(7, ChronoUnit.DAYS);
        break;
      case MONTH:
        startDate = now.minus(30, ChronoUnit.DAYS);
        break;
      case QUARTER:
        startDate = now.minus(90, ChronoUnit.DAYS);
        break;
      case YEAR:
        startDate = now.minus(365, ChronoUnit.DAYS);
        break;
      default:
        startDate = now.minus(30, ChronoUnit.DAYS); // Default to month
    }

    return new Instant[] { startDate, now };
  }

  @Override
  public StatisticsVm getActivityStatistics(TAccountRequest accountRequest) {
    // Calculate date ranges for statistics
    Instant now = Instant.now();
    Instant oneMonthAgo = now.minus(30, ChronoUnit.DAYS);
    Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);

    // Get total activities
    Long totalActivities = activityRepository.countTotalActivities();

    // Get total participants
    Long totalParticipants = activityRepository.countTotalParticipants();
    if (totalParticipants == null) {
      totalParticipants = 0L;
    }

    // Get activities in last month
    Long activitiesLastMonth = activityRepository.countActivitiesInDateRange(oneMonthAgo, now);

    // Get activities in last week
    Long activitiesLastWeek = activityRepository.countActivitiesInDateRange(oneWeekAgo, now);

    // Get average rating
    Double averageRating = feedbackRepository.getAverageRating();
    if (averageRating == null) {
      averageRating = 0.0;
    }

    // Get activities by category
    Map<String, Long> activitiesByCategory = new HashMap<>();
    activityRepository.countActivitiesByCategory().forEach(result -> {
      ActivityCategory category = (ActivityCategory) result[0];
      Long count = (Long) result[1];
      if (category != null && category.name() != null) {
        activitiesByCategory.put(category.name(), count != null ? count : 0L);
      }
    });

    // Ensure all categories are represented, even with zero count
    for (ActivityCategory category : ActivityCategory.values()) {
      if (category != null && category.name() != null) {
        activitiesByCategory.putIfAbsent(category.name(), 0L);
      }
    }

    // Get total reviews count
    Long totalReviews = feedbackRepository.countTotalReviews();

    // Get average score for each activity
    Map<Long, Double> averageScoreByActivity = new HashMap<>();
    feedbackRepository.getAverageRatingsByActivity().forEach(result -> {
      Long activityId = (Long) result[0];
      Double rating = (Double) result[1];
      if (activityId != null) {
        averageScoreByActivity.put(activityId, rating != null ? rating : 0.0);
      }
    });

    // Get top keywords from feedback - process in service layer
    List<KeywordCountVm> topKeywords = processKeywords(feedbackRepository.getAllFeedbackDescriptions());

    return StatisticsVm.builder()
        .totalActivities(totalActivities)
        .totalParticipants(totalParticipants)
        .activitiesLastMonth(activitiesLastMonth)
        .activitiesLastWeek(activitiesLastWeek)
        .averageRating(averageRating)
        .activitiesByCategory(activitiesByCategory)
        .totalReviews(totalReviews)
        .averageScoreByActivity(averageScoreByActivity)
        .topKeywords(topKeywords)
        .build();
  }

  /**
   * Process feedback descriptions to extract keywords
   */
  private List<KeywordCountVm> processKeywords(List<String> feedbackDescriptions) {
    List<KeywordCountVm> topKeywords = new ArrayList<>();

    if (feedbackDescriptions != null && !feedbackDescriptions.isEmpty()) {
      // Word frequency map
      Map<String, Long> wordFrequency = new HashMap<>();

      // Process each feedback
      for (String description : feedbackDescriptions) {
        if (description != null && !description.trim().isEmpty()) {
          // Split text into words and process each word
          String[] words = description.toLowerCase().split("\\W+");
          for (String word : words) {
            // Only count words with more than 3 characters and ignore common words
            if (word.length() > 3 && !isCommonWord(word)) {
              wordFrequency.put(word, wordFrequency.getOrDefault(word, 0L) + 1);
            }
          }
        }
      }

      // Convert to list and sort by frequency
      topKeywords = wordFrequency.entrySet().stream()
          .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
          .limit(MAX_KEYWORDS)
          .map(entry -> KeywordCountVm.builder()
              .keyword(entry.getKey())
              .count(entry.getValue())
              .build())
          .collect(Collectors.toList());
    }

    return topKeywords;
  }

  @Override
  public StatisticsVm getFilteredActivityStatistics(TAccountRequest accountRequest, StatisticsFilterDto filterDto) {
    // Initialize dates
    Instant startDate;
    Instant endDate;

    // Calculate start and end dates based on filter
    if (filterDto.getTimePeriod() != null && filterDto.getTimePeriod() != TimePeriod.CUSTOM) {
      // Use predefined time period
      Instant[] dateRange = calculateDateRange(filterDto.getTimePeriod());
      startDate = dateRange[0];
      endDate = dateRange[1];
    } else if (filterDto.getStartDate() != null && filterDto.getEndDate() != null) {
      // Use custom date range
      startDate = filterDto.getStartDate();
      endDate = filterDto.getEndDate();
    } else {
      // Default to last 30 days if no time period specified
      Instant now = Instant.now();
      startDate = now.minus(30, ChronoUnit.DAYS);
      endDate = now;
    }

    // Initialize statistics variables
    Long totalActivities;
    Long totalParticipants;
    Double averageRating;
    Long totalReviews;
    Map<String, Long> activitiesByCategory = new HashMap<>();
    Map<Long, Double> averageScoreByActivity = new HashMap<>();
    List<KeywordCountVm> topKeywords;

    // Apply filters for activity count
    if (filterDto.getActivityType() != null && filterDto.getStatus() != null) {
      // Filter by time, category, and status
      totalActivities = activityRepository.countActivitiesByTimeAndCategoryAndStatus(
          startDate, endDate, filterDto.getActivityType(), filterDto.getStatus());
    } else if (filterDto.getActivityType() != null) {
      // Filter by time and category
      totalActivities = activityRepository.countActivitiesByTimeAndCategory(
          startDate, endDate, filterDto.getActivityType());
    } else if (filterDto.getStatus() != null) {
      // Filter by time and status
      totalActivities = activityRepository.countActivitiesByTimeAndStatus(
          startDate, endDate, filterDto.getStatus());
    } else {
      // Filter by time only
      totalActivities = activityRepository.countActivitiesInDateRange(startDate, endDate);
    }

    // Apply filters for participant count
    if (filterDto.getActivityType() != null && filterDto.getStatus() != null) {
      // Filter by time, category, and status
      totalParticipants = activityRepository.countParticipantsByTimeAndCategoryAndStatus(
          startDate, endDate, filterDto.getActivityType(), filterDto.getStatus());
    } else if (filterDto.getActivityType() != null) {
      // Filter by time and category
      totalParticipants = activityRepository.countParticipantsByTimeAndCategory(
          startDate, endDate, filterDto.getActivityType());
    } else if (filterDto.getStatus() != null) {
      // Filter by time and status
      totalParticipants = activityRepository.countParticipantsByTimeAndStatus(
          startDate, endDate, filterDto.getStatus());
    } else {
      // Filter by time only
      totalParticipants = activityRepository.countParticipantsInTimeRange(startDate, endDate);
    }

    // Handle null participants
    if (totalParticipants == null) {
      totalParticipants = 0L;
    }

    // Get activities by category with time filter
    List<Object[]> categoryResults;
    if (filterDto.getTimePeriod() != null || (filterDto.getStartDate() != null && filterDto.getEndDate() != null)) {
      categoryResults = activityRepository.countActivitiesByCategoryInTimeRange(startDate, endDate);
    } else {
      categoryResults = activityRepository.countActivitiesByCategory();
    }

    // Process category results
    categoryResults.forEach(result -> {
      ActivityCategory category = (ActivityCategory) result[0];
      Long count = (Long) result[1];
      activitiesByCategory.put(category.name(), count);
    });

    // Ensure all categories are represented, even with zero count
    for (ActivityCategory category : ActivityCategory.values()) {
      if (!activitiesByCategory.containsKey(category.name())) {
        activitiesByCategory.put(category.name(), 0L);
      }
    }

    // Apply filters for reviews and ratings
    if (filterDto.getActivityType() != null && filterDto.getStatus() != null) {
      // Filter by time, category, and status
      averageRating = feedbackRepository.getAverageRatingByTimeAndCategoryAndStatus(
          startDate, endDate, filterDto.getActivityType(), filterDto.getStatus());
      totalReviews = feedbackRepository.countReviewsByTimeAndCategoryAndStatus(
          startDate, endDate, filterDto.getActivityType(), filterDto.getStatus());
      topKeywords = processKeywords(feedbackRepository.getFeedbackDescriptionsByTimeAndCategoryAndStatus(
          startDate, endDate, filterDto.getActivityType(), filterDto.getStatus()));
    } else if (filterDto.getActivityType() != null) {
      // Filter by time and category
      averageRating = feedbackRepository.getAverageRatingByTimeAndCategory(
          startDate, endDate, filterDto.getActivityType());
      totalReviews = feedbackRepository.countReviewsByTimeAndCategory(
          startDate, endDate, filterDto.getActivityType());
      topKeywords = processKeywords(feedbackRepository.getFeedbackDescriptionsByTimeAndCategory(
          startDate, endDate, filterDto.getActivityType()));
    } else if (filterDto.getStatus() != null) {
      // Filter by time and status
      averageRating = feedbackRepository.getAverageRatingByTimeAndStatus(
          startDate, endDate, filterDto.getStatus());
      totalReviews = feedbackRepository.countReviewsByTimeAndStatus(
          startDate, endDate, filterDto.getStatus());
      topKeywords = processKeywords(feedbackRepository.getFeedbackDescriptionsByTimeAndStatus(
          startDate, endDate, filterDto.getStatus()));
    } else {
      // Filter by time only
      averageRating = feedbackRepository.getAverageRatingInTimeRange(startDate, endDate);
      totalReviews = feedbackRepository.countReviewsInTimeRange(startDate, endDate);
      topKeywords = processKeywords(feedbackRepository.getFeedbackDescriptionsInTimeRange(startDate, endDate));
    }

    // Handle null ratings
    if (averageRating == null) {
      averageRating = 0.0;
    }

    // Handle null reviews
    if (totalReviews == null) {
      totalReviews = 0L;
    }

    // Build weekly and monthly statistics for comparison
    Instant now = Instant.now();
    Instant oneMonthAgo = now.minus(30, ChronoUnit.DAYS);
    Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);

    Long activitiesLastMonth = activityRepository.countActivitiesInDateRange(oneMonthAgo, now);
    Long activitiesLastWeek = activityRepository.countActivitiesInDateRange(oneWeekAgo, now);

    return StatisticsVm.builder()
        .totalActivities(totalActivities)
        .totalParticipants(totalParticipants)
        .activitiesLastMonth(activitiesLastMonth)
        .activitiesLastWeek(activitiesLastWeek)
        .averageRating(averageRating)
        .activitiesByCategory(activitiesByCategory)
        .totalReviews(totalReviews)
        .averageScoreByActivity(averageScoreByActivity) // This is not filtered in this implementation
        .topKeywords(topKeywords)
        .build();
  }
}
