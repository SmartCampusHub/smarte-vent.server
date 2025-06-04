package com.winnguyen1905.Activity.rest.service;

import java.time.Instant;
import java.util.List;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.model.dto.FeedbackCreateDto;
import com.winnguyen1905.Activity.model.dto.FeedbackUpdateDto;
import com.winnguyen1905.Activity.model.viewmodel.FeedbackDetailVm;
import com.winnguyen1905.Activity.model.viewmodel.FeedbackSummaryVm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {
    
    // Basic CRUD operations
    FeedbackDetailVm createFeedback(TAccountRequest accountRequest, FeedbackCreateDto feedbackDto);
    FeedbackDetailVm getFeedbackById(Long feedbackId);
    FeedbackDetailVm updateFeedback(TAccountRequest accountRequest, FeedbackUpdateDto feedbackDto);
    void deleteFeedback(Long feedbackId);
    
    // Student-specific operations
    Page<FeedbackSummaryVm> getStudentFeedbacks(Long studentId, Pageable pageable);
    boolean canStudentProvideFeedback(Long studentId, Long activityId);
    
    // Activity-specific operations
    Page<FeedbackSummaryVm> getActivityFeedbacks(Long activityId, Pageable pageable);
    Double getAverageRatingForActivity(Long activityId);
    
    // Organization-specific operations
    Page<FeedbackSummaryVm> getOrganizationFeedbacks(Long organizationId, Pageable pageable);
    Double getAverageRatingForOrganization(Long organizationId);
    List<Object[]> getAverageRatingByCategoryForOrganization(Long organizationId);
    List<Object[]> getRatingTrendByMonthForOrganization(Long organizationId, Instant startDate, Instant endDate);
    
    // Filtering operations
    Page<FeedbackSummaryVm> getFilteredFeedbacks(
            Instant startDate, 
            Instant endDate, 
            ActivityCategory category, 
            ActivityStatus status, 
            Long organizationId,
            Pageable pageable);
    
    // Feedback analytics
    List<String> getKeywordAnalysis(Long organizationId);
    List<Object[]> getBestRatedActivitiesForOrganization(Long organizationId, Long minFeedbacks, Pageable pageable);
}
