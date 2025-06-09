package com.winnguyen1905.Activity.rest.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.model.dto.FeedbackCreateDto;
import com.winnguyen1905.Activity.model.dto.FeedbackUpdateDto;
import com.winnguyen1905.Activity.model.dto.OrganizationResponseDto;
import com.winnguyen1905.Activity.model.viewmodel.FeedbackDetailVm;
import com.winnguyen1905.Activity.model.viewmodel.FeedbackSummaryVm;
import com.winnguyen1905.Activity.rest.service.FeedbackService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("feedbacks")
public class FeedbackController {

  @Autowired
  private FeedbackService feedbackService;

  // Student-specific endpoints

  /**
   * Create feedback for an activity
   * 
   * @param feedbackDto The feedback data
   * @return The created feedback
   */
  @PostMapping
  public ResponseEntity<FeedbackDetailVm> createFeedback(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody FeedbackCreateDto feedbackDto) {
    FeedbackDetailVm createdFeedback = feedbackService.createFeedback(accountRequest, feedbackDto);
    return new ResponseEntity<>(createdFeedback, HttpStatus.CREATED);
  }

  /**
   * Get all feedbacks provided by the current student
   * 
   * @param pageable Pagination parameters
   * @return Page of feedback summaries
   */
  @GetMapping("/my-feedbacks")
  public ResponseEntity<Page<FeedbackSummaryVm>> getMyFeedbacks(
      @AccountRequest TAccountRequest accountRequest,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<FeedbackSummaryVm> feedbacks = feedbackService.getStudentFeedbacks(accountRequest.id(), pageable);
    return ResponseEntity.ok(feedbacks);
  }

  /**
   * Check if the current student can provide feedback for an activity
   * 
   * @param activityId The activity ID
   * @return True if the student can provide feedback, false otherwise
   */
  @GetMapping("/can-provide/{activityId}")
  public ResponseEntity<Boolean> canProvideFeedback(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable Long activityId) {
    boolean canProvide = feedbackService.canStudentProvideFeedback(accountRequest.id(), activityId);
    return ResponseEntity.ok(canProvide);
  }

  /**
   * Update a feedback provided by the current student
   * 
   * @param feedbackDto The updated feedback data
   * @return The updated feedback
   */
  @PostMapping("/update")
  public ResponseEntity<FeedbackDetailVm> updateFeedback(
      @AccountRequest TAccountRequest accountRequest,
      @Valid @RequestBody FeedbackUpdateDto feedbackDto) {
    FeedbackDetailVm updatedFeedback = feedbackService.updateFeedback(accountRequest, feedbackDto);
    return ResponseEntity.ok(updatedFeedback);
  }

  /**
   * Delete a feedback provided by the current student
   * 
   * @param feedbackId The feedback ID
   * @return No content response
   */
  @DeleteMapping("/{feedbackId}")
  public ResponseEntity<Void> deleteFeedback(@PathVariable Long feedbackId) {
    feedbackService.deleteFeedback(feedbackId);
    return ResponseEntity.noContent().build();
  }

  // Activity-specific endpoints

  /**
   * Get all feedbacks for a specific activity
   * 
   * @param activityId The activity ID
   * @param pageable   Pagination parameters
   * @return Page of feedback summaries
   */
  @GetMapping("/activity/{activityId}")
  public ResponseEntity<Page<FeedbackSummaryVm>> getActivityFeedbacks(
      @PathVariable Long activityId,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<FeedbackSummaryVm> feedbacks = feedbackService.getActivityFeedbacks(activityId, pageable);
    return ResponseEntity.ok(feedbacks);
  }

  /**
   * Get the average rating for a specific activity
   * 
   * @param activityId The activity ID
   * @return The average rating
   */
  @GetMapping("/activity/{activityId}/average-rating")
  public ResponseEntity<Double> getActivityAverageRating(@PathVariable Long activityId) {
    Double averageRating = feedbackService.getAverageRatingForActivity(activityId);
    return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
  }

  // Organization-specific endpoints

  /**
   * Get all feedbacks for a specific organization
   * 
   * @param organizationId The organization ID
   * @param pageable       Pagination parameters
   * @return Page of feedback summaries
   */
  @GetMapping("/organization/{organizationId}")
  public ResponseEntity<Page<FeedbackSummaryVm>> getOrganizationFeedbacks(
      @PathVariable Long organizationId,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<FeedbackSummaryVm> feedbacks = feedbackService.getOrganizationFeedbacks(organizationId, pageable);
    return ResponseEntity.ok(feedbacks);
  }

  /**
   * Get the average rating for a specific organization
   * 
   * @param organizationId The organization ID
   * @return The average rating
   */
  @GetMapping("/organization/{organizationId}/average-rating")
  public ResponseEntity<Double> getOrganizationAverageRating(@PathVariable Long organizationId) {
    Double averageRating = feedbackService.getAverageRatingForOrganization(organizationId);
    return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
  }

  /**
   * Get the average rating by category for a specific organization
   * 
   * @param organizationId The organization ID
   * @return List of category and average rating pairs
   */
  @GetMapping("/organization/{organizationId}/rating-by-category")
  public ResponseEntity<List<Object[]>> getOrganizationRatingByCategory(@PathVariable Long organizationId) {
    List<Object[]> ratingsByCategory = feedbackService.getAverageRatingByCategoryForOrganization(organizationId);
    return ResponseEntity.ok(ratingsByCategory);
  }

  /**
   * Get the rating trend by month for a specific organization
   * 
   * @param organizationId The organization ID
   * @param startDate      The start date
   * @param endDate        The end date
   * @return List of year, month, and average rating triples
   */
  @GetMapping("/organization/{organizationId}/rating-trend")
  public ResponseEntity<List<Object[]>> getOrganizationRatingTrend(
      @PathVariable Long organizationId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
    List<Object[]> ratingTrend = feedbackService.getRatingTrendByMonthForOrganization(organizationId, startDate,
        endDate);
    return ResponseEntity.ok(ratingTrend);
  }

  /**
   * Get the keyword analysis for a specific organization
   * 
   * @param organizationId The organization ID
   * @return List of keywords
   */
  @GetMapping("/organization/{organizationId}/keyword-analysis")
  public ResponseEntity<List<String>> getOrganizationKeywordAnalysis(@PathVariable Long organizationId) {
    List<String> keywords = feedbackService.getKeywordAnalysis(organizationId);
    return ResponseEntity.ok(keywords);
  }

  /**
   * Get the best rated activities for a specific organization
   * 
   * @param organizationId The organization ID
   * @param minFeedbacks   The minimum number of feedbacks (default: 3)
   * @param pageable       Pagination parameters
   * @return List of activity and average rating pairs
   */
  @GetMapping("/organization/{organizationId}/best-activities")
  public ResponseEntity<List<Object[]>> getOrganizationBestActivities(
      @PathVariable Long organizationId,
      @RequestParam(defaultValue = "3") Long minFeedbacks,
      @PageableDefault(size = 5) Pageable pageable) {
    List<Object[]> bestActivities = feedbackService.getBestRatedActivitiesForOrganization(organizationId,
        minFeedbacks, pageable);
    return ResponseEntity.ok(bestActivities);
  }

  // Filter endpoints

  /**
   * Get filtered feedbacks
   * 
   * @param startDate      The start date (optional)
   * @param endDate        The end date (optional)
   * @param category       The activity category (optional)
   * @param status         The activity status (optional)
   * @param organizationId The organization ID (optional)
   * @param pageable       Pagination parameters
   * @return Page of feedback summaries
   */
  @GetMapping("/filter")
  public ResponseEntity<Page<FeedbackSummaryVm>> getFilteredFeedbacks(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
      @RequestParam(required = false) ActivityCategory category,
      @RequestParam(required = false) ActivityStatus status,
      @RequestParam(required = false) Long organizationId,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<FeedbackSummaryVm> feedbacks = feedbackService.getFilteredFeedbacks(startDate, endDate, category, status,
        organizationId, pageable);
    return ResponseEntity.ok(feedbacks);
  }

  // Admin-specific endpoints

  /**
   * Get a specific feedback by ID
   * 
   * @param feedbackId The feedback ID
   * @return The feedback details
   */
  @GetMapping("/{feedbackId}")
  public ResponseEntity<FeedbackDetailVm> getFeedbackById(@PathVariable Long feedbackId) {
    FeedbackDetailVm feedback = feedbackService.getFeedbackById(feedbackId);
    return ResponseEntity.ok(feedback);
  }

  /**
   * Add organization response to a feedback
   * 
   * @param feedbackId  The feedback ID
   * @param responseDto The organization response data
   * @return The updated feedback
   */
  @PostMapping("/{feedbackId}/respond")
  public ResponseEntity<FeedbackDetailVm> respondToFeedback(
      @PathVariable Long feedbackId,
      @Valid @RequestBody OrganizationResponseDto responseDto) {
    FeedbackDetailVm updatedFeedback = feedbackService.addOrganizationResponse(feedbackId, responseDto);
    return ResponseEntity.ok(updatedFeedback);
  }
}
