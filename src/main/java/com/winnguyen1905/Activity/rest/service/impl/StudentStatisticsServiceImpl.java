package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.viewmodel.ParticipationSummaryVm;
import com.winnguyen1905.activity.model.viewmodel.StudentStatisticsVm;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.rest.service.StudentStatisticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for generating student-specific statistics.
 * Provides comprehensive analytics for individual students including participation metrics,
 * activity history, performance indicators, and trend analysis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentStatisticsServiceImpl implements StudentStatisticsService {

    private final ParticipationDetailRepository participationDetailRepository;
    private final AccountRepository accountRepository;

    // Configuration constants
    private static final int RECENT_ACTIVITIES_LIMIT = 5;
    private static final int TREND_MONTHS_LIMIT = 12;
    
    private static final String STUDENT_NOT_FOUND = "Student not found with ID: %d";

    @Override
    public StudentStatisticsVm getMyStatistics(TAccountRequest accountRequest) {
        log.debug("Retrieving statistics for current student: {}", accountRequest.getId());
        return getStudentStatisticsById(accountRequest.getId());
    }

    @Override
    public StudentStatisticsVm getStudentStatistics(TAccountRequest accountRequest, Long studentId) {
        log.debug("Retrieving statistics for student: {} requested by: {}", studentId, accountRequest.getId());
        
        // TODO: Implement permission checks
        // Only administrators or organization managers should be allowed to view other students' statistics
        
        return getStudentStatisticsById(studentId);
    }

    /**
     * Internal method to gather comprehensive statistics for a student by ID.
     *
     * @param studentId The ID of the student to gather statistics for
     * @return Complete student statistics view model
     * @throws ResourceNotFoundException if the student is not found
     */
    private StudentStatisticsVm getStudentStatisticsById(Long studentId) {
        log.debug("Generating statistics for student ID: {}", studentId);
        
        EAccountCredentials student = validateAndGetStudent(studentId);
        
        DateRange trendPeriod = calculateTrendPeriod();
        
        return StudentStatisticsVm.builder()
            .studentId(studentId)
            .studentName(student.getFullName())
            .totalActivitiesParticipated(getTotalParticipatedActivities(studentId))
            .activitiesAsVolunteer(getActivitiesByRole(studentId, ParticipationRole.CONTRIBUTOR))
            .activitiesAsParticipant(getActivitiesByRole(studentId, ParticipationRole.PARTICIPANT))
            .totalParticipationHours(getTotalParticipationHours(studentId))
            .averageAssessmentScore(getAverageAssessmentScore(studentId))
            .totalTrainingScore(getTotalTrainingScore(studentId))
            .activitiesByCategory(getActivitiesByCategory(studentId))
            .recentActivities(getRecentActivities(studentId))
            .monthlyParticipationTrend(getMonthlyParticipationTrend(studentId, trendPeriod))
            .build();
    }

    /**
     * Validates student existence and retrieves the student entity.
     *
     * @param studentId The student ID to validate
     * @return The validated student entity
     * @throws ResourceNotFoundException if the student is not found
     */
    private EAccountCredentials validateAndGetStudent(Long studentId) {
        return accountRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(STUDENT_NOT_FOUND, studentId)));
    }

    /**
     * Calculates the date range for trend analysis.
     *
     * @return DateRange object with start and end dates for trend analysis
     */
    private DateRange calculateTrendPeriod() {
        LocalDate nowDate = LocalDate.now();
        LocalDate oneYearAgoDate = nowDate.minusMonths(TREND_MONTHS_LIMIT);
        
        Instant now = nowDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant oneYearAgo = oneYearAgoDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        return new DateRange(oneYearAgo, now);
    }

    /**
     * Gets the total number of activities the student has participated in.
     *
     * @param studentId The student ID
     * @return Total count of participated activities
     */
    private Long getTotalParticipatedActivities(Long studentId) {
        Long count = participationDetailRepository.countTotalActivitiesByStudentId(studentId);
        return count != null ? count : 0L;
    }

    /**
     * Gets the number of activities by participation role.
     *
     * @param studentId The student ID
     * @param role The participation role to filter by
     * @return Count of activities for the specified role
     */
    private Long getActivitiesByRole(Long studentId, ParticipationRole role) {
        Long count = participationDetailRepository.countActivitiesByStudentIdAndRole(studentId, role);
        return count != null ? count : 0L;
    }

    /**
     * Calculates total participation hours for the student.
     *
     * @param studentId The student ID
     * @return Total participation hours
     */
    private Double getTotalParticipationHours(Long studentId) {
        Double hours = participationDetailRepository.calculateTotalParticipationHours(studentId);
        return hours != null ? hours : 0.0;
    }

    /**
     * Gets the average assessment score for the student.
     *
     * @param studentId The student ID
     * @return Average assessment score
     */
    private Double getAverageAssessmentScore(Long studentId) {
        Double score = participationDetailRepository.getAverageAssessmentScoreByStudentId(studentId);
        return score != null ? score : 0.0;
    }

    /**
     * Gets the total training score for the student.
     *
     * @param studentId The student ID
     * @return Total training score
     */
    private Double getTotalTrainingScore(Long studentId) {
        Double score = participationDetailRepository.getTotalTrainingScoreByStudentId(studentId);
        return score != null ? score : 0.0;
    }

    /**
     * Gets activities grouped by category for the student.
     *
     * @param studentId The student ID
     * @return Map of category names to activity counts
     */
    private Map<String, Long> getActivitiesByCategory(Long studentId) {
        Map<String, Long> activitiesByCategory = new HashMap<>();
        
        participationDetailRepository.getActivitiesByCategory(studentId).forEach(result -> {
            ActivityCategory category = (ActivityCategory) result[0];
            Long count = (Long) result[1];
            activitiesByCategory.put(category.name(), count);
        });
        
        return activitiesByCategory;
    }

    /**
     * Gets recent activities for the student.
     *
     * @param studentId The student ID
     * @return List of recent participation summaries
     */
    private List<ParticipationSummaryVm> getRecentActivities(Long studentId) {
        List<EParticipationDetail> recentActivities = participationDetailRepository
                .getRecentActivitiesByStudentId(studentId, PageRequest.of(0, RECENT_ACTIVITIES_LIMIT));

        return recentActivities.stream()
                .map(this::convertToParticipationSummary)
                .collect(Collectors.toList());
    }

    /**
     * Gets monthly participation trend for the student.
     *
     * @param studentId The student ID
     * @param trendPeriod The date range for trend analysis
     * @return Map of month keys to participation counts
     */
    private Map<String, Long> getMonthlyParticipationTrend(Long studentId, DateRange trendPeriod) {
        Map<String, Long> monthlyTrend = new HashMap<>();
        
        participationDetailRepository.getMonthlyParticipationTrend(studentId, trendPeriod.start(), trendPeriod.end())
                .forEach(result -> {
                    Integer year = (Integer) result[0];
                    Integer month = (Integer) result[1];
                    Long count = (Long) result[2];

                    String monthKey = formatMonthKey(year, month);
                    monthlyTrend.put(monthKey, count);
                });
        
        return monthlyTrend;
    }

    /**
     * Converts an EParticipationDetail entity to a ParticipationSummaryVm.
     *
     * @param participationDetail The participation detail entity
     * @return The converted participation summary view model
     */
    private ParticipationSummaryVm convertToParticipationSummary(EParticipationDetail participationDetail) {
        return ParticipationSummaryVm.builder()
                .participationId(participationDetail.getId())
                .activityId(getActivityId(participationDetail))
                .activityName(getActivityName(participationDetail))
                .activityCategory(getActivityCategory(participationDetail))
                .participationRole(participationDetail.getParticipationRole())
                .participationStatus(participationDetail.getParticipationStatus())
                .participationDate(participationDetail.getRegisteredAt())
                .hoursSpent(calculateHoursSpent(participationDetail))
                .assessmentScore(calculateAssessmentScore(participationDetail))
                .build();
    }

    /**
     * Safely extracts activity ID from participation detail.
     */
    private Long getActivityId(EParticipationDetail participationDetail) {
        return participationDetail.getActivity() != null ? participationDetail.getActivity().getId() : null;
    }

    /**
     * Safely extracts activity name from participation detail.
     */
    private String getActivityName(EParticipationDetail participationDetail) {
        return participationDetail.getActivity() != null 
            ? participationDetail.getActivity().getActivityName() 
            : "Unknown Activity";
    }

    /**
     * Safely extracts activity category from participation detail.
     */
    private ActivityCategory getActivityCategory(EParticipationDetail participationDetail) {
        return participationDetail.getActivity() != null 
            ? participationDetail.getActivity().getActivityCategory() 
            : null;
    }

    /**
     * Calculates hours spent based on activity duration.
     *
     * @param participationDetail The participation detail containing activity information
     * @return Hours spent in the activity
     */
    private Double calculateHoursSpent(EParticipationDetail participationDetail) {
        if (participationDetail.getActivity() == null ||
            participationDetail.getActivity().getStartDate() == null ||
            participationDetail.getActivity().getEndDate() == null) {
            return 0.0;
        }

        long durationMillis = participationDetail.getActivity().getEndDate().toEpochMilli() -
                participationDetail.getActivity().getStartDate().toEpochMilli();
        
        return durationMillis / (1000.0 * 60 * 60); // Convert milliseconds to hours
    }

    /**
     * Calculates assessment score from available feedback.
     *
     * @param participationDetail The participation detail containing feedback information
     * @return Average assessment score or null if no feedback available
     */
    private Double calculateAssessmentScore(EParticipationDetail participationDetail) {
        if (participationDetail.getFeedbacks() == null || participationDetail.getFeedbacks().isEmpty()) {
            return null;
        }

        return participationDetail.getFeedbacks().stream()
                .filter(feedback -> feedback.getRating() != null)
                .mapToDouble(feedback -> feedback.getRating())
                .average()
                .orElse(0.0);
    }

    /**
     * Formats year and month into a standard month key string.
     *
     * @param year The year
     * @param month The month (1-12)
     * @return Formatted month key (YYYY-MM)
     */
    private String formatMonthKey(Integer year, Integer month) {
        return String.format("%d-%02d", year, month);
    }

    /**
     * Record class for holding date range information.
     */
    private record DateRange(Instant start, Instant end) {}
}
