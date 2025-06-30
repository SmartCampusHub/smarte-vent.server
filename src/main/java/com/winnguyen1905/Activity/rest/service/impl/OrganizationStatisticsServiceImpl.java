package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.TimePeriod;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.ActivityStatisticsSummaryVm;
import com.winnguyen1905.activity.model.viewmodel.OrganizationStatisticsVm;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EOrganization;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.activity.persistance.repository.OrganizationRepository;
import com.winnguyen1905.activity.rest.service.OrganizationStatisticsService;
import com.winnguyen1905.activity.rest.service.AuthorizationService;
import com.winnguyen1905.activity.utils.DateTimeUtils;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for generating organization-specific statistics.
 * Provides comprehensive analytics for organizations including activity metrics,
 * participation rates, performance indicators, and trend analysis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationStatisticsServiceImpl implements OrganizationStatisticsService {

  private final ActivityRepository activityRepository;
  private final FeedbackRepository feedbackRepository;
  private final OrganizationRepository organizationRepository;
  private final AuthorizationService authorizationService;

  // Configuration constants
  private static final int TOP_ITEMS_COUNT = 5;
  private static final long MIN_FEEDBACKS_FOR_RATING = 3;
  private static final int TREND_MONTHS_LIMIT = 12;
  
  private static final String ORGANIZATION_NOT_FOUND = "Organization not found with ID: %d";

  @Override
  public OrganizationStatisticsVm getOrganizationStatistics(Long organizationId) {
    log.debug("Generating statistics for organization ID: {}", organizationId);
    
    EOrganization organization = validateAndGetOrganization(organizationId);
    OrganizationStatisticsVm statistics = buildBaseStatistics(organization);
    
    // Populate all statistics sections
    enrichStatisticsWithMetrics(statistics, organizationId);
    
    log.debug("Successfully generated statistics for organization ID: {}", organizationId);
    return statistics;
  }

  /**
   * Authorization-aware version of getOrganizationStatistics
   */
  public OrganizationStatisticsVm getOrganizationStatistics(Long organizationId, TAccountRequest accountRequest) {
    // Authorization check: Only admins or the organization itself can view statistics
    authorizationService.validateOrganizationAccess(organizationId, accountRequest);
    
    return getOrganizationStatistics(organizationId);
  }

  @Override
  public OrganizationStatisticsVm getFilteredOrganizationStatistics(Long organizationId, StatisticsFilterDto filter) {
    log.debug("Generating filtered statistics for organization ID: {} with filter: {}", organizationId, filter);
    
    DateRange dateRange = calculateDateRangeFromFilter(filter);
    
    if (dateRange != null) {
      return getOrganizationStatisticsInDateRange(organizationId, dateRange.startDate(), dateRange.endDate());
    }
    
    return getOrganizationStatistics(organizationId);
  }

  @Override
  public OrganizationStatisticsVm getDailyOrganizationStatistics(Long organizationId) {
    return getStatisticsForTimePeriod(organizationId, TimePeriod.DAY);
  }

  @Override
  public OrganizationStatisticsVm getWeeklyOrganizationStatistics(Long organizationId) {
    return getStatisticsForTimePeriod(organizationId, TimePeriod.WEEK);
  }

  @Override
  public OrganizationStatisticsVm getMonthlyOrganizationStatistics(Long organizationId) {
    return getStatisticsForTimePeriod(organizationId, TimePeriod.MONTH);
  }

  @Override
  public OrganizationStatisticsVm getQuarterlyOrganizationStatistics(Long organizationId) {
    return getStatisticsForTimePeriod(organizationId, TimePeriod.QUARTER);
  }

  @Override
  public OrganizationStatisticsVm getYearlyOrganizationStatistics(Long organizationId) {
    return getStatisticsForTimePeriod(organizationId, TimePeriod.YEAR);
  }

  @Override
  public OrganizationStatisticsVm getOrganizationStatisticsInDateRange(Long organizationId, 
                                                                       Instant startDate, 
                                                                       Instant endDate) {
    log.debug("Generating statistics for organization ID: {} from {} to {}", organizationId, startDate, endDate);
    
    // For now, return overall statistics. 
    // A full implementation would filter all statistics based on the date range
    return getOrganizationStatistics(organizationId);
  }

  /**
   * Validates organization existence and retrieves it.
   *
   * @param organizationId The organization ID to validate
   * @return The validated organization entity
   * @throws ResourceNotFoundException if organization is not found
   */
  private EOrganization validateAndGetOrganization(Long organizationId) {
    return organizationRepository.findById(organizationId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(ORGANIZATION_NOT_FOUND, organizationId)));
  }

  /**
   * Builds base statistics structure with organization information.
   *
   * @param organization The organization entity
   * @return Base statistics view model
   */
  private OrganizationStatisticsVm buildBaseStatistics(EOrganization organization) {
    return OrganizationStatisticsVm.builder()
        .organizationId(organization.getId())
        .organizationName(organization.getName())
        .organizationType(organization.getType() != null ? organization.getType().toString() : null)
        .build();
  }

  /**
   * Enriches statistics with all metrics and analytics.
   *
   * @param statistics The statistics view model to enrich
   * @param organizationId The organization ID
   */
  private void enrichStatisticsWithMetrics(OrganizationStatisticsVm statistics, Long organizationId) {
    populateActivityMetrics(statistics, organizationId);
    populateParticipationMetrics(statistics, organizationId);
    populatePerformanceMetrics(statistics, organizationId);
    populateCategoryBreakdowns(statistics, organizationId);
    populateTimeBasedMetrics(statistics, organizationId);
    populateTopPerformingItems(statistics, organizationId);
  }

  /**
   * Populates activity-related metrics.
   */
  private void populateActivityMetrics(OrganizationStatisticsVm statistics, Long organizationId) {
    statistics.setTotalActivities(safeLongValue(
        activityRepository.countTotalActivitiesByOrganization(organizationId)));
    statistics.setUpcomingActivities(safeLongValue(
        activityRepository.countUpcomingActivitiesByOrganization(organizationId)));
    statistics.setOngoingActivities(safeLongValue(
        activityRepository.countOngoingActivitiesByOrganization(organizationId)));
    statistics.setCompletedActivities(safeLongValue(
        activityRepository.countCompletedActivitiesByOrganization(organizationId)));
    statistics.setCanceledActivities(safeLongValue(
        activityRepository.countActivitiesByStatusAndOrganization(organizationId, 
            com.winnguyen1905.activity.common.constant.ActivityStatus.CANCELLED)));
  }

  /**
   * Populates participation-related metrics.
   */
  private void populateParticipationMetrics(OrganizationStatisticsVm statistics, Long organizationId) {
    statistics.setTotalParticipants(safeLongValue(
        activityRepository.countTotalParticipantsByOrganization(organizationId)));
    statistics.setAverageParticipantsPerActivity(safeDoubleValue(
        activityRepository.calculateAverageParticipantsPerActivityByOrganization(organizationId)));
    statistics.setParticipationRate(safeDoubleValue(
        activityRepository.calculateParticipationRateByOrganization(organizationId)));
  }

  /**
   * Populates performance-related metrics.
   */
  private void populatePerformanceMetrics(OrganizationStatisticsVm statistics, Long organizationId) {
    statistics.setAverageFeedbackRating(safeDoubleValue(
        feedbackRepository.getAverageRatingForOrganization(organizationId)));
    statistics.setTotalFeedbacks(safeLongValue(
        feedbackRepository.countTotalFeedbacksForOrganization(organizationId)));
  }

  /**
   * Populates category breakdown metrics.
   */
  private void populateCategoryBreakdowns(OrganizationStatisticsVm statistics, Long organizationId) {
    statistics.setActivitiesByCategory(buildCategoryMap(
        activityRepository.getActivitiesByCategoryForOrganization(organizationId)));
    statistics.setParticipantsByCategory(buildCategoryMap(
        activityRepository.getParticipantsByCategoryForOrganization(organizationId)));
  }

  /**
   * Populates time-based trend metrics.
   */
  private void populateTimeBasedMetrics(OrganizationStatisticsVm statistics, Long organizationId) {
    Instant end = Instant.now();
    Instant start = end.minus(365, ChronoUnit.DAYS);

    statistics.setActivitiesByMonth(buildMonthlyMap(
        activityRepository.getActivitiesByMonthForOrganization(organizationId, start, end)));
    statistics.setParticipantsByMonth(buildMonthlyMap(
        activityRepository.getParticipantsByMonthForOrganization(organizationId, start, end)));
  }

  /**
   * Populates top-performing activities and best-rated activities.
   */
  private void populateTopPerformingItems(OrganizationStatisticsVm statistics, Long organizationId) {
    populateTopActivities(statistics, organizationId);
    populateBestRatedActivities(statistics, organizationId);
  }

  /**
   * Populates top activities by participation.
   */
  private void populateTopActivities(OrganizationStatisticsVm statistics, Long organizationId) {
    List<EActivity> topActivities = activityRepository.getTopActivitiesByParticipationForOrganization(
        organizationId, PageRequest.of(0, TOP_ITEMS_COUNT));

    List<ActivityStatisticsSummaryVm> topActivitiesVm = topActivities.stream()
        .map(this::mapActivityToSummary)
        .collect(Collectors.toList());
    
    statistics.setTopActivities(topActivitiesVm);
  }

  /**
   * Populates best-rated activities.
   */
  private void populateBestRatedActivities(OrganizationStatisticsVm statistics, Long organizationId) {
    List<Object[]> bestRatedActivitiesData = feedbackRepository.getBestRatedActivitiesForOrganization(
        organizationId, MIN_FEEDBACKS_FOR_RATING, PageRequest.of(0, TOP_ITEMS_COUNT));

    List<ActivityStatisticsSummaryVm> bestRatedActivitiesVm = bestRatedActivitiesData.stream()
        .map(this::mapBestRatedActivityData)
        .collect(Collectors.toList());
    
    statistics.setBestRatedActivities(bestRatedActivitiesVm);
  }

  /**
   * Gets statistics for a specific time period.
   */
  private OrganizationStatisticsVm getStatisticsForTimePeriod(Long organizationId, TimePeriod timePeriod) {
    StatisticsFilterDto filter = new StatisticsFilterDto();
    filter.setTimePeriod(timePeriod);
    return getFilteredOrganizationStatistics(organizationId, filter);
  }

  /**
   * Calculates date range from statistics filter.
   */
  private DateRange calculateDateRangeFromFilter(StatisticsFilterDto filter) {
    if (filter.getTimePeriod() != null) {
      return calculateDateRangeFromTimePeriod(filter.getTimePeriod());
    }
    
    if (filter.getStartDate() != null && filter.getEndDate() != null) {
      return new DateRange(filter.getStartDate(), filter.getEndDate());
    }
    
    return null;
  }

  /**
   * Calculates date range from time period enum.
   */
  private DateRange calculateDateRangeFromTimePeriod(TimePeriod timePeriod) {
    return switch (timePeriod) {
      case DAY -> new DateRange(DateTimeUtils.getStartOfCurrentDay(), DateTimeUtils.getEndOfCurrentDay());
      case WEEK -> new DateRange(DateTimeUtils.getStartOfCurrentWeek(), DateTimeUtils.getEndOfCurrentWeek());
      case MONTH -> new DateRange(DateTimeUtils.getStartOfCurrentMonth(), DateTimeUtils.getEndOfCurrentMonth());
      case QUARTER -> new DateRange(DateTimeUtils.getStartOfCurrentQuarter(), DateTimeUtils.getEndOfCurrentQuarter());
      case YEAR -> new DateRange(DateTimeUtils.getStartOfCurrentYear(), DateTimeUtils.getEndOfCurrentYear());
      default -> null;
    };
  }

  /**
   * Builds a category map from repository results.
   */
  private Map<String, Long> buildCategoryMap(List<Object[]> categoryData) {
    Map<String, Long> categoryMap = new HashMap<>();
    
    categoryData.forEach(data -> {
      ActivityCategory category = (ActivityCategory) data[0];
      Long count = ((Number) data[1]).longValue();
      categoryMap.put(category.toString(), count);
    });
    
    return categoryMap;
  }

  /**
   * Builds a monthly map from repository results.
   */
  private Map<String, Long> buildMonthlyMap(List<Object[]> monthlyData) {
    Map<String, Long> monthlyMap = new HashMap<>();
    
    monthlyData.forEach(data -> {
      String monthKey = buildMonthKey((Number) data[0], (Number) data[1]);
      Long count = ((Number) data[2]).longValue();
      monthlyMap.put(monthKey, count);
    });
    
    return monthlyMap;
  }

  /**
   * Maps an activity entity to summary view model.
   */
  private ActivityStatisticsSummaryVm mapActivityToSummary(EActivity activity) {
    ActivityStatisticsSummaryVm summary = ActivityStatisticsSummaryVm.builder()
        .activityId(activity.getId())
        .activityName(activity.getActivityName())
        .category(activity.getActivityCategory())
        .status(activity.getStatus())
        .startDate(activity.getStartDate())
        .endDate(activity.getEndDate())
        .capacityLimit(activity.getCapacityLimit())
        .currentParticipants(activity.getCurrentParticipants())
        .build();

    enrichSummaryWithCalculatedMetrics(summary, activity);
    return summary;
  }

  /**
   * Maps best-rated activity data to summary view model.
   */
  private ActivityStatisticsSummaryVm mapBestRatedActivityData(Object[] data) {
    EActivity activity = (EActivity) data[0];
    Double avgRating = (Double) data[1];

    ActivityStatisticsSummaryVm summary = mapActivityToSummary(activity);
    summary.setAverageRating(avgRating);
    summary.setFeedbackCount(safeLongValue(feedbackRepository.countByActivityId(activity.getId())));
    
    return summary;
  }

  /**
   * Enriches activity summary with calculated metrics.
   */
  private void enrichSummaryWithCalculatedMetrics(ActivityStatisticsSummaryVm summary, EActivity activity) {
    // Calculate participation rate
    if (activity.getCapacityLimit() > 0) {
      double participationRate = (activity.getCurrentParticipants() * 100.0) / activity.getCapacityLimit();
      summary.setParticipationRate(participationRate);
    } else {
      summary.setParticipationRate(0.0);
    }

    // Get average rating and feedback count
    Double averageRating = feedbackRepository.getAverageRatingForActivity(activity.getId());
    summary.setAverageRating(averageRating != null ? averageRating : 0.0);

    Long feedbackCount = feedbackRepository.countByActivityId(activity.getId());
    summary.setFeedbackCount(feedbackCount != null ? feedbackCount : 0L);
  }

  /**
   * Safely converts Number to Long with null protection.
   */
  private long safeLongValue(Number number) {
    return number != null ? number.longValue() : 0L;
  }

  /**
   * Safely converts Number to Double with null protection.
   */
  private double safeDoubleValue(Number number) {
    return number != null ? number.doubleValue() : 0.0;
  }

  /**
   * Builds a month key string from year and month numbers.
   */
  private String buildMonthKey(Number year, Number month) {
    int m = month.intValue();
    return year + "-" + (m < 10 ? "0" + m : Integer.toString(m));
  }

  /**
   * Record class for holding date range information.
   */
  private record DateRange(Instant startDate, Instant endDate) {}
}
