package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.TimePeriod;
import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.KeywordCountVm;
import com.winnguyen1905.activity.model.viewmodel.StatisticsVm;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.activity.rest.service.StatisticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for generating activity statistics.
 * Provides comprehensive analytics including activity counts, participant metrics,
 * feedback analysis, and keyword extraction from user feedback.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

  private final ActivityRepository activityRepository;
  private final FeedbackRepository feedbackRepository;

  // Configuration constants
  private static final int MAX_KEYWORDS = 10;
  private static final int MIN_KEYWORD_LENGTH = 3;
  private static final int DEFAULT_PERIOD_DAYS = 30;

  // Common words to exclude from keyword analysis
  private static final Set<String> COMMON_WORDS = Set.of(
      "this", "that", "these", "those", "with", "from", "have", "has", "had",
      "what", "when", "where", "which", "who", "whom", "whose", "why", "how",
      "there", "here", "were", "their", "they", "them", "then", "than",
      "your", "you", "our", "ours", "about", "would", "could", "should",
      "will", "shall", "may", "might", "must", "can", "such", "like", "just",
      "and", "but", "for", "not", "the", "are", "was", "been", "very", "more"
  );

  @Override
  public StatisticsVm getActivityStatistics(TAccountRequest accountRequest) {
    log.debug("Generating activity statistics for account: {}", accountRequest.getId());
    
    Instant[] dateRanges = calculateCommonDateRanges();
    Instant oneMonthAgo = dateRanges[0];
    Instant oneWeekAgo = dateRanges[1];
    Instant now = dateRanges[2];

    return StatisticsVm.builder()
        .totalActivities(getTotalActivities())
        .totalParticipants(getTotalParticipants())
        .activitiesLastMonth(getActivitiesInPeriod(oneMonthAgo, now))
        .activitiesLastWeek(getActivitiesInPeriod(oneWeekAgo, now))
        .averageRating(getAverageRating())
        .activitiesByCategory(getActivitiesByCategory(null, null))
        .totalReviews(getTotalReviews())
        .averageScoreByActivity(getAverageScoreByActivity())
        .topKeywords(extractTopKeywords())
        .build();
  }

  @Override
  public StatisticsVm getFilteredActivityStatistics(TAccountRequest accountRequest, StatisticsFilterDto filterDto) {
    log.debug("Generating filtered activity statistics with filter: {}", filterDto);
    
    DateRange dateRange = calculateDateRange(filterDto);
    
    return StatisticsVm.builder()
        .totalActivities(getFilteredActivitiesCount(filterDto, dateRange))
        .totalParticipants(getFilteredParticipantsCount(filterDto, dateRange))
        .activitiesLastMonth(getActivitiesInPeriod(dateRange.start().minus(30, ChronoUnit.DAYS), dateRange.end()))
        .activitiesLastWeek(getActivitiesInPeriod(dateRange.start().minus(7, ChronoUnit.DAYS), dateRange.end()))
        .averageRating(getFilteredAverageRating(filterDto, dateRange))
        .activitiesByCategory(getActivitiesByCategory(dateRange.start(), dateRange.end()))
        .totalReviews(getFilteredTotalReviews(filterDto, dateRange))
        .averageScoreByActivity(getAverageScoreByActivity()) // Not filtered in current implementation
        .topKeywords(getFilteredTopKeywords(filterDto, dateRange))
        .build();
  }

  /**
   * Calculates common date ranges used across statistics.
   * 
   * @return Array containing [oneMonthAgo, oneWeekAgo, now]
   */
  private Instant[] calculateCommonDateRanges() {
    Instant now = Instant.now();
    Instant oneMonthAgo = now.minus(30, ChronoUnit.DAYS);
    Instant oneWeekAgo = now.minus(7, ChronoUnit.DAYS);
    return new Instant[]{oneMonthAgo, oneWeekAgo, now};
  }

  /**
   * Calculates date range based on filter criteria.
   * 
   * @param filterDto The filter containing time period or custom dates
   * @return DateRange object with start and end dates
   */
  private DateRange calculateDateRange(StatisticsFilterDto filterDto) {
    if (filterDto.getTimePeriod() != null && filterDto.getTimePeriod() != TimePeriod.CUSTOM) {
      return calculateDateRangeFromTimePeriod(filterDto.getTimePeriod());
    }
    
    if (filterDto.getStartDate() != null && filterDto.getEndDate() != null) {
      return new DateRange(filterDto.getStartDate(), filterDto.getEndDate());
    }
    
    // Default to last 30 days
    Instant now = Instant.now();
    return new DateRange(now.minus(DEFAULT_PERIOD_DAYS, ChronoUnit.DAYS), now);
  }

  /**
   * Calculates date range from predefined time period.
   * 
   * @param timePeriod The time period enum
   * @return DateRange object with calculated dates
   */
  private DateRange calculateDateRangeFromTimePeriod(TimePeriod timePeriod) {
    Instant now = Instant.now();
    Instant startDate = switch (timePeriod) {
      case DAY -> now.minus(1, ChronoUnit.DAYS);
      case WEEK -> now.minus(7, ChronoUnit.DAYS);
      case MONTH -> now.minus(30, ChronoUnit.DAYS);
      case QUARTER -> now.minus(90, ChronoUnit.DAYS);
      case YEAR -> now.minus(365, ChronoUnit.DAYS);
      default -> now.minus(DEFAULT_PERIOD_DAYS, ChronoUnit.DAYS);
    };
    return new DateRange(startDate, now);
  }

  /**
   * Gets total activities count with null safety.
   */
  private Long getTotalActivities() {
    Long count = activityRepository.countTotalActivities();
    return count != null ? count : 0L;
  }

  /**
   * Gets total participants count with null safety.
   */
  private Long getTotalParticipants() {
    Long count = activityRepository.countTotalParticipants();
    return count != null ? count : 0L;
  }

  /**
   * Gets activities count in a specific time period.
   */
  private Long getActivitiesInPeriod(Instant startDate, Instant endDate) {
    Long count = activityRepository.countActivitiesInDateRange(startDate, endDate);
    return count != null ? count : 0L;
  }

  /**
   * Gets average rating with null safety.
   */
  private Double getAverageRating() {
    Double rating = feedbackRepository.getAverageRating();
    return rating != null ? rating : 0.0;
  }

  /**
   * Gets total reviews count with null safety.
   */
  private Long getTotalReviews() {
    Long count = feedbackRepository.countTotalReviews();
    return count != null ? count : 0L;
  }

  /**
   * Gets activities grouped by category with proper initialization.
   */
  private Map<String, Long> getActivitiesByCategory(Instant startDate, Instant endDate) {
    Map<String, Long> activitiesByCategory = new HashMap<>();
    
    // Initialize all categories with zero count
    Arrays.stream(ActivityCategory.values())
        .forEach(category -> activitiesByCategory.put(category.name(), 0L));
    
    // Get actual counts
    List<Object[]> categoryResults = (startDate != null && endDate != null) 
        ? activityRepository.countActivitiesByCategoryInTimeRange(startDate, endDate)
        : activityRepository.countActivitiesByCategory();
    
    // Update with actual values
    categoryResults.forEach(result -> {
      ActivityCategory category = (ActivityCategory) result[0];
      Long count = (Long) result[1];
      if (category != null && category.name() != null) {
        activitiesByCategory.put(category.name(), count != null ? count : 0L);
      }
    });
    
    return activitiesByCategory;
  }

  /**
   * Gets average score by activity.
   */
  private Map<Long, Double> getAverageScoreByActivity() {
    Map<Long, Double> averageScoreByActivity = new HashMap<>();
    feedbackRepository.getAverageRatingsByActivity().forEach(result -> {
      Long activityId = (Long) result[0];
      Double rating = (Double) result[1];
      if (activityId != null) {
        averageScoreByActivity.put(activityId, rating != null ? rating : 0.0);
      }
    });
    return averageScoreByActivity;
  }

  /**
   * Extracts top keywords from all feedback descriptions.
   */
  private List<KeywordCountVm> extractTopKeywords() {
    List<String> feedbackDescriptions = feedbackRepository.getAllFeedbackDescriptions();
    return processKeywords(feedbackDescriptions);
  }

  /**
   * Gets filtered activities count based on filter criteria.
   */
  private Long getFilteredActivitiesCount(StatisticsFilterDto filterDto, DateRange dateRange) {
    if (hasActivityTypeAndStatus(filterDto)) {
      return activityRepository.countActivitiesByTimeAndCategoryAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getActivityType(), filterDto.getStatus());
    }
    
    if (filterDto.getActivityType() != null) {
      return activityRepository.countActivitiesByTimeAndCategory(
          dateRange.start(), dateRange.end(), filterDto.getActivityType());
    }
    
    if (filterDto.getStatus() != null) {
      return activityRepository.countActivitiesByTimeAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getStatus());
    }
    
    return getActivitiesInPeriod(dateRange.start(), dateRange.end());
  }

  /**
   * Gets filtered participants count based on filter criteria.
   */
  private Long getFilteredParticipantsCount(StatisticsFilterDto filterDto, DateRange dateRange) {
    Long count = null;
    
    if (hasActivityTypeAndStatus(filterDto)) {
      count = activityRepository.countParticipantsByTimeAndCategoryAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getActivityType(), filterDto.getStatus());
    } else if (filterDto.getActivityType() != null) {
      count = activityRepository.countParticipantsByTimeAndCategory(
          dateRange.start(), dateRange.end(), filterDto.getActivityType());
    } else if (filterDto.getStatus() != null) {
      count = activityRepository.countParticipantsByTimeAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getStatus());
    } else {
      count = activityRepository.countParticipantsInTimeRange(dateRange.start(), dateRange.end());
    }
    
    return count != null ? count : 0L;
  }

  /**
   * Gets filtered average rating based on filter criteria.
   */
  private Double getFilteredAverageRating(StatisticsFilterDto filterDto, DateRange dateRange) {
    Double rating = null;
    
    if (hasActivityTypeAndStatus(filterDto)) {
      rating = feedbackRepository.getAverageRatingByTimeAndCategoryAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getActivityType(), filterDto.getStatus());
    } else if (filterDto.getActivityType() != null) {
      rating = feedbackRepository.getAverageRatingByTimeAndCategory(
          dateRange.start(), dateRange.end(), filterDto.getActivityType());
    } else if (filterDto.getStatus() != null) {
      rating = feedbackRepository.getAverageRatingByTimeAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getStatus());
    } else {
      rating = feedbackRepository.getAverageRatingInTimeRange(dateRange.start(), dateRange.end());
    }
    
    return rating != null ? rating : 0.0;
  }

  /**
   * Gets filtered total reviews count based on filter criteria.
   */
  private Long getFilteredTotalReviews(StatisticsFilterDto filterDto, DateRange dateRange) {
    Long count = null;
    
    if (hasActivityTypeAndStatus(filterDto)) {
      count = feedbackRepository.countReviewsByTimeAndCategoryAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getActivityType(), filterDto.getStatus());
    } else if (filterDto.getActivityType() != null) {
      count = feedbackRepository.countReviewsByTimeAndCategory(
          dateRange.start(), dateRange.end(), filterDto.getActivityType());
    } else if (filterDto.getStatus() != null) {
      count = feedbackRepository.countReviewsByTimeAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getStatus());
    } else {
      count = feedbackRepository.countReviewsInTimeRange(dateRange.start(), dateRange.end());
    }
    
    return count != null ? count : 0L;
  }

  /**
   * Gets filtered top keywords based on filter criteria.
   */
  private List<KeywordCountVm> getFilteredTopKeywords(StatisticsFilterDto filterDto, DateRange dateRange) {
    List<String> descriptions = null;
    
    if (hasActivityTypeAndStatus(filterDto)) {
      descriptions = feedbackRepository.getFeedbackDescriptionsByTimeAndCategoryAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getActivityType(), filterDto.getStatus());
    } else if (filterDto.getActivityType() != null) {
      descriptions = feedbackRepository.getFeedbackDescriptionsByTimeAndCategory(
          dateRange.start(), dateRange.end(), filterDto.getActivityType());
    } else if (filterDto.getStatus() != null) {
      descriptions = feedbackRepository.getFeedbackDescriptionsByTimeAndStatus(
          dateRange.start(), dateRange.end(), filterDto.getStatus());
    } else {
      descriptions = feedbackRepository.getFeedbackDescriptionsInTimeRange(dateRange.start(), dateRange.end());
    }
    
    return processKeywords(descriptions);
  }

  /**
   * Checks if filter has both activity type and status criteria.
   */
  private boolean hasActivityTypeAndStatus(StatisticsFilterDto filterDto) {
    return filterDto.getActivityType() != null && filterDto.getStatus() != null;
  }

  /**
   * Processes feedback descriptions to extract and rank keywords.
   * 
   * @param feedbackDescriptions List of feedback text descriptions
   * @return List of top keywords with their counts
   */
  private List<KeywordCountVm> processKeywords(List<String> feedbackDescriptions) {
    if (feedbackDescriptions == null || feedbackDescriptions.isEmpty()) {
      return List.of();
    }

    Map<String, Long> wordFrequency = new HashMap<>();
    
    feedbackDescriptions.stream()
        .filter(description -> description != null && !description.trim().isEmpty())
        .forEach(description -> extractWordsFromDescription(description, wordFrequency));

    return wordFrequency.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(MAX_KEYWORDS)
        .map(entry -> KeywordCountVm.builder()
            .keyword(entry.getKey())
            .count(entry.getValue())
            .build())
        .collect(Collectors.toList());
  }

  /**
   * Extracts words from a single feedback description and updates frequency map.
   * 
   * @param description The feedback description text
   * @param wordFrequency The frequency map to update
   */
  private void extractWordsFromDescription(String description, Map<String, Long> wordFrequency) {
    String[] words = description.toLowerCase().split("\\W+");
    
    Arrays.stream(words)
        .filter(word -> word.length() > MIN_KEYWORD_LENGTH)
        .filter(word -> !COMMON_WORDS.contains(word))
        .forEach(word -> wordFrequency.merge(word, 1L, Long::sum));
  }

  /**
   * Record class for holding date range information.
   */
  private record DateRange(Instant start, Instant end) {}
}
