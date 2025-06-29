package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.common.constant.TimePeriod;
import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.ActivityStatisticsSummaryVm;
import com.winnguyen1905.activity.model.viewmodel.OrganizationStatisticsVm;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EOrganization;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.activity.persistance.repository.OrganizationRepository;
import com.winnguyen1905.activity.rest.service.OrganizationStatisticsService;
import com.winnguyen1905.activity.utils.DateTimeUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationStatisticsServiceImpl implements OrganizationStatisticsService {

  private final ActivityRepository activityRepository;
  private final FeedbackRepository feedbackRepository;
  private final OrganizationRepository organizationRepository;

  private static final int TOP_ITEMS_COUNT = 5;
  private static final long MIN_FEEDBACKS_FOR_RATING = 3;

  @Override
  public OrganizationStatisticsVm getOrganizationStatistics(Long organizationId) {
    EOrganization organization = validateAndGetOrganization(organizationId);

    OrganizationStatisticsVm statistics = buildBaseStatistics(organization);

    populateActivityStats(statistics, organizationId);
    populateParticipationStats(statistics, organizationId);
    populatePerformanceMetrics(statistics, organizationId);
    populateCategoryBreakdown(statistics, organizationId);
    populateTimeBasedMetrics(statistics, organizationId);
    populateTopActivities(statistics, organizationId);
    populateBestRatedActivities(statistics, organizationId);

    return statistics;
  }

  @Override
  public OrganizationStatisticsVm getFilteredOrganizationStatistics(Long organizationId, StatisticsFilterDto filter) {
    Instant startDate = null;
    Instant endDate = null;

    if (filter.getTimePeriod() != null) {
      switch (filter.getTimePeriod()) {
        case DAY:
          startDate = DateTimeUtils.getStartOfCurrentDay();
          endDate = DateTimeUtils.getEndOfCurrentDay();
          break;
        case WEEK:
          startDate = DateTimeUtils.getStartOfCurrentWeek();
          endDate = DateTimeUtils.getEndOfCurrentWeek();
          break;
        case MONTH:
          startDate = DateTimeUtils.getStartOfCurrentMonth();
          endDate = DateTimeUtils.getEndOfCurrentMonth();
          break;
        case QUARTER:
          startDate = DateTimeUtils.getStartOfCurrentQuarter();
          endDate = DateTimeUtils.getEndOfCurrentQuarter();
          break;
        case YEAR:
          startDate = DateTimeUtils.getStartOfCurrentYear();
          endDate = DateTimeUtils.getEndOfCurrentYear();
          break;
        default:
          break;
      }
    } else if (filter.getStartDate() != null && filter.getEndDate() != null) {
      startDate = filter.getStartDate();
      endDate = filter.getEndDate();
    }

    return getOrganizationStatisticsInDateRange(organizationId, startDate, endDate);
  }

  @Override
  public OrganizationStatisticsVm getDailyOrganizationStatistics(Long organizationId) {
    StatisticsFilterDto filter = new StatisticsFilterDto();
    filter.setTimePeriod(TimePeriod.DAY);
    return getFilteredOrganizationStatistics(organizationId, filter);
  }

  @Override
  public OrganizationStatisticsVm getWeeklyOrganizationStatistics(Long organizationId) {
    StatisticsFilterDto filter = new StatisticsFilterDto();
    filter.setTimePeriod(TimePeriod.WEEK);
    return getFilteredOrganizationStatistics(organizationId, filter);
  }

  @Override
  public OrganizationStatisticsVm getMonthlyOrganizationStatistics(Long organizationId) {
    StatisticsFilterDto filter = new StatisticsFilterDto();
    filter.setTimePeriod(TimePeriod.MONTH);
    return getFilteredOrganizationStatistics(organizationId, filter);
  }

  @Override
  public OrganizationStatisticsVm getQuarterlyOrganizationStatistics(Long organizationId) {
    StatisticsFilterDto filter = new StatisticsFilterDto();
    filter.setTimePeriod(TimePeriod.QUARTER);
    return getFilteredOrganizationStatistics(organizationId, filter);
  }

  @Override
  public OrganizationStatisticsVm getYearlyOrganizationStatistics(Long organizationId) {
    StatisticsFilterDto filter = new StatisticsFilterDto();
    filter.setTimePeriod(TimePeriod.YEAR);
    return getFilteredOrganizationStatistics(organizationId, filter);
  }

  @Override
  public OrganizationStatisticsVm getOrganizationStatisticsInDateRange(Long organizationId, Instant startDate,
      Instant endDate) {
    // For now, just return the overall statistics
    // A full implementation would filter all the statistics based on the date range
    return getOrganizationStatistics(organizationId);
  }

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

    // Calculate participation rate
    if (activity.getCapacityLimit() > 0) {
      double participationRate = (activity.getCurrentParticipants() * 100.0) / activity.getCapacityLimit();
      summary.setParticipationRate(participationRate);
    } else {
      summary.setParticipationRate(0.0);
    }

    // Get average rating for this activity
    Double averageRating = feedbackRepository.getAverageRatingForActivity(activity.getId());
    summary.setAverageRating(averageRating != null ? averageRating : 0.0);

    // Get feedback count for this activity
    Long feedbackCount = feedbackRepository.countByActivityId(activity.getId());
    summary.setFeedbackCount(feedbackCount != null ? feedbackCount : 0L);

    return summary;
  }

  /* ===================== PRIVATE HELPER METHODS ===================== */

  private EOrganization validateAndGetOrganization(Long organizationId) {
    return organizationRepository.findById(organizationId)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
  }

  private OrganizationStatisticsVm buildBaseStatistics(EOrganization organization) {
    return OrganizationStatisticsVm.builder()
        .organizationId(organization.getId())
        .organizationName(organization.getName())
        .organizationType(organization.getType() != null ? organization.getType().toString() : null)
        .build();
  }

  private void populateActivityStats(OrganizationStatisticsVm statistics, Long organizationId) {
    statistics.setTotalActivities(safeLong(activityRepository.countTotalActivitiesByOrganization(organizationId)));
    statistics.setUpcomingActivities(safeLong(activityRepository.countUpcomingActivitiesByOrganization(organizationId)));
    statistics.setOngoingActivities(safeLong(activityRepository.countOngoingActivitiesByOrganization(organizationId)));
    statistics.setCompletedActivities(safeLong(activityRepository.countCompletedActivitiesByOrganization(organizationId)));
    statistics.setCanceledActivities(safeLong(activityRepository.countActivitiesByStatusAndOrganization(organizationId, ActivityStatus.CANCELLED)));
  }

  private void populateParticipationStats(OrganizationStatisticsVm statistics, Long organizationId) {
    statistics.setTotalParticipants(safeLong(activityRepository.countTotalParticipantsByOrganization(organizationId)));
    statistics.setAverageParticipantsPerActivity(safeDouble(activityRepository.calculateAverageParticipantsPerActivityByOrganization(organizationId)));
    statistics.setParticipationRate(safeDouble(activityRepository.calculateParticipationRateByOrganization(organizationId)));
  }

  private void populatePerformanceMetrics(OrganizationStatisticsVm statistics, Long organizationId) {
    statistics.setAverageFeedbackRating(safeDouble(feedbackRepository.getAverageRatingForOrganization(organizationId)));
    statistics.setTotalFeedbacks(safeLong(feedbackRepository.countTotalFeedbacksForOrganization(organizationId)));
  }

  private void populateCategoryBreakdown(OrganizationStatisticsVm statistics, Long organizationId) {
    Map<String, Long> activitiesByCategory = new HashMap<>();
    for (Object[] data : activityRepository.getActivitiesByCategoryForOrganization(organizationId)) {
      activitiesByCategory.put(((ActivityCategory) data[0]).toString(), ((Number) data[1]).longValue());
    }
    statistics.setActivitiesByCategory(activitiesByCategory);

    Map<String, Long> participantsByCategory = new HashMap<>();
    for (Object[] data : activityRepository.getParticipantsByCategoryForOrganization(organizationId)) {
      participantsByCategory.put(((ActivityCategory) data[0]).toString(), ((Number) data[1]).longValue());
    }
    statistics.setParticipantsByCategory(participantsByCategory);
  }

  private void populateTimeBasedMetrics(OrganizationStatisticsVm statistics, Long organizationId) {
    Instant end = Instant.now();
    Instant start = end.minus(365, ChronoUnit.DAYS);

    Map<String, Long> activitiesByMonth = new HashMap<>();
    for (Object[] data : activityRepository.getActivitiesByMonthForOrganization(organizationId, start, end)) {
      String monthKey = buildMonthKey((Number) data[0], (Number) data[1]);
      activitiesByMonth.put(monthKey, ((Number) data[2]).longValue());
    }
    statistics.setActivitiesByMonth(activitiesByMonth);

    Map<String, Long> participantsByMonth = new HashMap<>();
    for (Object[] data : activityRepository.getParticipantsByMonthForOrganization(organizationId, start, end)) {
      String monthKey = buildMonthKey((Number) data[0], (Number) data[1]);
      participantsByMonth.put(monthKey, ((Number) data[2]).longValue());
    }
    statistics.setParticipantsByMonth(participantsByMonth);
  }

  private void populateTopActivities(OrganizationStatisticsVm statistics, Long organizationId) {
    List<EActivity> topActivities = activityRepository.getTopActivitiesByParticipationForOrganization(
        organizationId, PageRequest.of(0, TOP_ITEMS_COUNT));

    List<ActivityStatisticsSummaryVm> topActivitiesVm = topActivities.stream()
        .map(this::mapActivityToSummary)
        .collect(Collectors.toList());
    statistics.setTopActivities(topActivitiesVm);
  }

  private void populateBestRatedActivities(OrganizationStatisticsVm statistics, Long organizationId) {
    List<Object[]> bestRatedActivitiesData = feedbackRepository.getBestRatedActivitiesForOrganization(
        organizationId, MIN_FEEDBACKS_FOR_RATING, PageRequest.of(0, TOP_ITEMS_COUNT));

    List<ActivityStatisticsSummaryVm> bestRatedActivitiesVm = new ArrayList<>();
    for (Object[] data : bestRatedActivitiesData) {
      EActivity activity = (EActivity) data[0];
      Double avgRating = (Double) data[1];

      ActivityStatisticsSummaryVm summary = mapActivityToSummary(activity);
      summary.setAverageRating(avgRating);
      summary.setFeedbackCount(safeLong(feedbackRepository.countByActivityId(activity.getId())));

      bestRatedActivitiesVm.add(summary);
    }
    statistics.setBestRatedActivities(bestRatedActivitiesVm);
  }

  private long safeLong(Number number) {
    return number != null ? number.longValue() : 0L;
  }

  private double safeDouble(Number number) {
    return number != null ? number.doubleValue() : 0.0;
  }

  private String buildMonthKey(Number year, Number month) {
    int m = month.intValue();
    return year + "-" + (m < 10 ? "0" + m : Integer.toString(m));
  }
}
