package com.winnguyen1905.Activity.rest.service.impl;

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

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.TimePeriod;
import com.winnguyen1905.Activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityStatisticsSummaryVm;
import com.winnguyen1905.Activity.model.viewmodel.OrganizationStatisticsVm;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EOrganization;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.Activity.persistance.repository.OrganizationRepository;
import com.winnguyen1905.Activity.rest.service.OrganizationStatisticsService;
import com.winnguyen1905.Activity.utils.DateTimeUtils;

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
        Optional<EOrganization> organizationOpt = organizationRepository.findById(organizationId);
        if (!organizationOpt.isPresent()) {
            throw new IllegalArgumentException("Organization not found: " + organizationId);
        }

        EOrganization organization = organizationOpt.get();

        OrganizationStatisticsVm statistics = OrganizationStatisticsVm.builder()
                .organizationId(organization.getId())
                .organizationName(organization.getName())
                .organizationType(organization.getType() != null ? organization.getType().toString() : null)
                .build();

        // Activity statistics
        Long totalActivities = activityRepository.countTotalActivitiesByOrganization(organizationId);
        Long upcomingActivities = activityRepository.countUpcomingActivitiesByOrganization(organizationId);
        Long ongoingActivities = activityRepository.countOngoingActivitiesByOrganization(organizationId);
        Long completedActivities = activityRepository.countCompletedActivitiesByOrganization(organizationId);
        Long canceledActivities = activityRepository.countActivitiesByStatusAndOrganization(organizationId,
                ActivityStatus.CANCELLED);

        statistics.setTotalActivities(totalActivities != null ? totalActivities : 0L);
        statistics.setUpcomingActivities(upcomingActivities != null ? upcomingActivities : 0L);
        statistics.setOngoingActivities(ongoingActivities != null ? ongoingActivities : 0L);
        statistics.setCompletedActivities(completedActivities != null ? completedActivities : 0L);
        statistics.setCanceledActivities(canceledActivities != null ? canceledActivities : 0L);

        // Participation statistics
        Long totalParticipants = activityRepository.countTotalParticipantsByOrganization(organizationId);
        Double averageParticipantsPerActivity = activityRepository
                .calculateAverageParticipantsPerActivityByOrganization(organizationId);
        Double participationRate = activityRepository.calculateParticipationRateByOrganization(organizationId);

        statistics.setTotalParticipants(totalParticipants != null ? totalParticipants : 0L);
        statistics.setAverageParticipantsPerActivity(
                averageParticipantsPerActivity != null ? averageParticipantsPerActivity : 0.0);
        statistics.setParticipationRate(participationRate != null ? participationRate : 0.0);

        // Performance metrics
        Double averageRating = feedbackRepository.getAverageRatingForOrganization(organizationId);
        Long totalFeedbacks = feedbackRepository.countTotalFeedbacksForOrganization(organizationId);

        statistics.setAverageFeedbackRating(averageRating != null ? averageRating : 0.0);
        statistics.setTotalFeedbacks(totalFeedbacks != null ? totalFeedbacks : 0L);

        // Category breakdown
        Map<String, Long> activitiesByCategory = new HashMap<>();
        List<Object[]> activityCategoryData = activityRepository.getActivitiesByCategoryForOrganization(organizationId);
        for (Object[] data : activityCategoryData) {
            ActivityCategory category = (ActivityCategory) data[0];
            Long count = (Long) data[1];
            activitiesByCategory.put(category.toString(), count);
        }
        statistics.setActivitiesByCategory(activitiesByCategory);

        Map<String, Long> participantsByCategory = new HashMap<>();
        List<Object[]> participantCategoryData = activityRepository
                .getParticipantsByCategoryForOrganization(organizationId);
        for (Object[] data : participantCategoryData) {
            ActivityCategory category = (ActivityCategory) data[0];
            Long count = ((Number) data[1]).longValue();
            participantsByCategory.put(category.toString(), count);
        }
        statistics.setParticipantsByCategory(participantsByCategory);

        // Time-based metrics
        Instant oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS);
        Instant now = Instant.now();

        Map<String, Long> activitiesByMonth = new HashMap<>();
        List<Object[]> activityMonthData = activityRepository.getActivitiesByMonthForOrganization(organizationId,
                oneYearAgo, now);
        for (Object[] data : activityMonthData) {
            Integer year = ((Number) data[0]).intValue();
            Integer month = ((Number) data[1]).intValue();
            Long count = (Long) data[2];
            String monthKey = year + "-" + (month < 10 ? "0" + month : month);
            activitiesByMonth.put(monthKey, count);
        }
        statistics.setActivitiesByMonth(activitiesByMonth);

        Map<String, Long> participantsByMonth = new HashMap<>();
        List<Object[]> participantMonthData = activityRepository.getParticipantsByMonthForOrganization(organizationId,
                oneYearAgo, now);
        for (Object[] data : participantMonthData) {
            Integer year = ((Number) data[0]).intValue();
            Integer month = ((Number) data[1]).intValue();
            Long count = ((Number) data[2]).longValue();
            String monthKey = year + "-" + (month < 10 ? "0" + month : month);
            participantsByMonth.put(monthKey, count);
        }
        statistics.setParticipantsByMonth(participantsByMonth);

        // Top activities
        List<EActivity> topActivities = activityRepository.getTopActivitiesByParticipationForOrganization(
                organizationId, PageRequest.of(0, TOP_ITEMS_COUNT));

        List<ActivityStatisticsSummaryVm> topActivitiesVm = topActivities.stream()
                .map(activity -> mapActivityToSummary(activity))
                .collect(Collectors.toList());
        statistics.setTopActivities(topActivitiesVm);

        // Best rated activities
        List<Object[]> bestRatedActivitiesData = feedbackRepository.getBestRatedActivitiesForOrganization(
                organizationId, MIN_FEEDBACKS_FOR_RATING, PageRequest.of(0, TOP_ITEMS_COUNT));

        List<ActivityStatisticsSummaryVm> bestRatedActivitiesVm = new ArrayList<>();
        for (Object[] data : bestRatedActivitiesData) {
            EActivity activity = (EActivity) data[0];
            Double avgRating = (Double) data[1];

            ActivityStatisticsSummaryVm summary = mapActivityToSummary(activity);
            summary.setAverageRating(avgRating);

            Long feedbackCount = feedbackRepository.countByActivityId(activity.getId());
            summary.setFeedbackCount(feedbackCount);

            bestRatedActivitiesVm.add(summary);
        }
        statistics.setBestRatedActivities(bestRatedActivitiesVm);

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
}
