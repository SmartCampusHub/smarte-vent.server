package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

@Service
@RequiredArgsConstructor
public class StudentStatisticsServiceImpl implements StudentStatisticsService {

    private final ParticipationDetailRepository participationDetailRepository;
    private final AccountRepository accountRepository;

    private static final int RECENT_ACTIVITIES_LIMIT = 5;
    private static final int TREND_MONTHS_LIMIT = 12;

    @Override
    public StudentStatisticsVm getMyStatistics(TAccountRequest accountRequest) {
        Long studentId = accountRequest.getId();
        return getStudentStatisticsById(studentId);
    }

    @Override
    public StudentStatisticsVm getStudentStatistics(TAccountRequest accountRequest, Long studentId) {
        // TODO: Check if the requester has permission to view this student's statistics
        // Only administrators or organization managers should be allowed

        return getStudentStatisticsById(studentId);
    }

    /**
     * Internal method to gather statistics for a student by ID
     */
    private StudentStatisticsVm getStudentStatisticsById(Long studentId) {
        // Verify student exists
        EAccountCredentials student = accountRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // Calculate time range for trends (past 12 months)
        LocalDate nowDate = LocalDate.now();
        LocalDate oneYearAgoDate = nowDate.minusMonths(TREND_MONTHS_LIMIT);
        
        // Convert LocalDate to Instant for repository queries
        Instant now = nowDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant oneYearAgo = oneYearAgoDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Get total participation counts
        Long totalActivities = participationDetailRepository.countTotalActivitiesByStudentId(studentId);
        Long activitiesAsVolunteer = participationDetailRepository.countActivitiesByStudentIdAndRole(
                studentId, ParticipationRole.CONTRIBUTOR);
        Long activitiesAsParticipant = participationDetailRepository.countActivitiesByStudentIdAndRole(
                studentId, ParticipationRole.PARTICIPANT);

        // Get total participation hours
        Double totalHours = participationDetailRepository.calculateTotalParticipationHours(studentId);
        if (totalHours == null) {
            totalHours = 0.0;
        }

        // Get average assessment score
        Double averageScore = participationDetailRepository.getAverageAssessmentScoreByStudentId(studentId);
        if (averageScore == null) {
            averageScore = 0.0;
        }

        // Get total training score
        Double trainingScore = participationDetailRepository.getTotalTrainingScoreByStudentId(studentId);
        if (trainingScore == null) {
            trainingScore = 0.0;
        }

        // Get activities by category
        Map<String, Long> activitiesByCategory = new HashMap<>();
        participationDetailRepository.getActivitiesByCategory(studentId).forEach(result -> {
            ActivityCategory category = (ActivityCategory) result[0];
            Long count = (Long) result[1];
            activitiesByCategory.put(category.name(), count);
        });

        // Get recent activities
        List<EParticipationDetail> recentActivities = participationDetailRepository.getRecentActivitiesByStudentId(
                studentId, PageRequest.of(0, RECENT_ACTIVITIES_LIMIT));

        List<ParticipationSummaryVm> recentActivitySummaries = recentActivities.stream()
                .map(this::convertToSummary)
                .collect(Collectors.toList());

        // Get monthly participation trend
        Map<String, Long> monthlyTrend = new HashMap<>();
        participationDetailRepository.getMonthlyParticipationTrend(studentId, oneYearAgo, now).forEach(result -> {
            Integer year = (Integer) result[0];
            Integer month = (Integer) result[1];
            Long count = (Long) result[2];

            // Format as "YYYY-MM"
            String monthKey = String.format("%d-%02d", year, month);
            monthlyTrend.put(monthKey, count);
        });

        // Build the statistics view model
        return StudentStatisticsVm.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .totalActivitiesParticipated(totalActivities)
                .activitiesAsVolunteer(activitiesAsVolunteer)
                .activitiesAsParticipant(activitiesAsParticipant)
                .totalParticipationHours(totalHours)
                .averageAssessmentScore(averageScore)
                .totalTrainingScore(trainingScore)
                .activitiesByCategory(activitiesByCategory)
                .recentActivities(recentActivitySummaries)
                .monthlyParticipationTrend(monthlyTrend)
                .build();
    }

    /**
     * Convert an EParticipationDetail entity to a ParticipationSummaryVm
     */
    private ParticipationSummaryVm convertToSummary(EParticipationDetail detail) {
        // Calculate hours spent based on activity duration
        Double hoursSpent = 0.0;
        if (detail.getActivity() != null &&
                detail.getActivity().getStartDate() != null &&
                detail.getActivity().getEndDate() != null) {

            long durationMillis = detail.getActivity().getEndDate().toEpochMilli() -
                    detail.getActivity().getStartDate().toEpochMilli();
            hoursSpent = durationMillis / (1000.0 * 60 * 60); // Convert milliseconds to hours
        }

        // Get assessment score if available
        Double assessmentScore = null;
        if (detail.getFeedbacks() != null && !detail.getFeedbacks().isEmpty()) {
            assessmentScore = detail.getFeedbacks().stream()
                    .filter(f -> f.getRating() != null)
                    .mapToDouble(f -> f.getRating())
                    .average()
                    .orElse(0.0);
        }

        return ParticipationSummaryVm.builder()
                .participationId(detail.getId())
                .activityId(detail.getActivity() != null ? detail.getActivity().getId() : null)
                .activityName(
                        detail.getActivity() != null ? detail.getActivity().getActivityName() : "Unknown Activity")
                .activityCategory(detail.getActivity() != null ? detail.getActivity().getActivityCategory() : null)
                .participationRole(detail.getParticipationRole())
                .participationStatus(detail.getParticipationStatus())
                .participationDate(detail.getRegisteredAt())
                .hoursSpent(hoursSpent)
                .assessmentScore(assessmentScore)
                .build();
    }
}
