package com.winnguyen1905.activity.rest.controller;

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

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.model.dto.FeedbackCreateDto;
import com.winnguyen1905.activity.model.dto.FeedbackUpdateDto;
import com.winnguyen1905.activity.model.dto.OrganizationResponseDto;
import com.winnguyen1905.activity.model.viewmodel.FeedbackDetailVm;
import com.winnguyen1905.activity.model.viewmodel.FeedbackSummaryVm;
import com.winnguyen1905.activity.rest.service.FeedbackService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("feedbacks")
@Tag(name = "Feedback Management", description = "Operations for managing activity and organization feedback")
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
  @Operation(summary = "Create feedback", description = "Create feedback for an activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Feedback created successfully", 
                content = @Content(schema = @Schema(implementation = FeedbackDetailVm.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<FeedbackDetailVm> createFeedback(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Feedback data", required = true) @RequestBody FeedbackCreateDto feedbackDto) {
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
  @Operation(summary = "Get my feedbacks", description = "Get all feedbacks provided by the current student")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Feedbacks retrieved successfully", 
                content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<Page<FeedbackSummaryVm>> getMyFeedbacks(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
    Page<FeedbackSummaryVm> feedbacks = feedbackService.getStudentFeedbacks(accountRequest.getId(), pageable);
    return ResponseEntity.ok(feedbacks);
  }

  /**
   * Check if the current student can provide feedback for an activity
   * 
   * @param activityId The activity ID
   * @return True if the student can provide feedback, false otherwise
   */
  @GetMapping("/can-provide/{activityId}")
  @Operation(summary = "Check if can provide feedback", description = "Check if the current student can provide feedback for an activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Check completed successfully", 
                content = @Content(schema = @Schema(implementation = Boolean.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<Boolean> canProvideFeedback(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "ID of the activity to check", required = true) @PathVariable Long activityId) {
    boolean canProvide = feedbackService.canStudentProvideFeedback(accountRequest.getId(), activityId);
    return ResponseEntity.ok(canProvide);
  }

  /**
   * Update a feedback provided by the current student
   * 
   * @param feedbackDto The updated feedback data
   * @return The updated feedback
   */
  @PostMapping("/update")
  @Operation(summary = "Update feedback", description = "Update a feedback provided by the current student")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Feedback updated successfully", 
                content = @Content(schema = @Schema(implementation = FeedbackDetailVm.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Feedback not found")
  })
  public ResponseEntity<FeedbackDetailVm> updateFeedback(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Updated feedback data", required = true) @Valid @RequestBody FeedbackUpdateDto feedbackDto) {
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
  @Operation(summary = "Delete feedback", description = "Delete a feedback provided by the current student")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Feedback deleted successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Feedback not found")
  })
  public ResponseEntity<Void> deleteFeedback(
      @Parameter(description = "ID of the feedback to delete", required = true) @PathVariable Long feedbackId) {
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
  @Operation(summary = "Get activity feedbacks", description = "Get all feedbacks for a specific activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Feedbacks retrieved successfully", 
                content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<Page<FeedbackSummaryVm>> getActivityFeedbacks(
      @Parameter(description = "ID of the activity", required = true) @PathVariable Long activityId,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
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
  @Operation(summary = "Get activity average rating", description = "Get the average rating for a specific activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Average rating retrieved successfully", 
                content = @Content(schema = @Schema(implementation = Double.class))),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<Double> getActivityAverageRating(
      @Parameter(description = "ID of the activity", required = true) @PathVariable Long activityId) {
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
  @Operation(summary = "Get organization feedbacks", description = "Get all feedbacks for a specific organization")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Feedbacks retrieved successfully", 
                content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public ResponseEntity<Page<FeedbackSummaryVm>> getOrganizationFeedbacks(
      @Parameter(description = "ID of the organization", required = true) @PathVariable Long organizationId,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
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
  @Operation(summary = "Get organization average rating", description = "Get the average rating for a specific organization")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Average rating retrieved successfully", 
                content = @Content(schema = @Schema(implementation = Double.class))),
    @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public ResponseEntity<Double> getOrganizationAverageRating(
      @Parameter(description = "ID of the organization", required = true) @PathVariable Long organizationId) {
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
  @Operation(summary = "Get organization rating by category", description = "Get the average rating by category for a specific organization")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Category ratings retrieved successfully", 
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Object[].class)))),
    @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public ResponseEntity<List<Object[]>> getOrganizationRatingByCategory(
      @Parameter(description = "ID of the organization", required = true) @PathVariable Long organizationId) {
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
  @Operation(summary = "Get organization rating trend", description = "Get the rating trend by month for a specific organization")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Rating trend retrieved successfully", 
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Object[].class)))),
    @ApiResponse(responseCode = "400", description = "Invalid date range"),
    @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public ResponseEntity<List<Object[]>> getOrganizationRatingTrend(
      @Parameter(description = "ID of the organization", required = true) @PathVariable Long organizationId,
      @Parameter(description = "Start date for the trend analysis", required = true) 
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @Parameter(description = "End date for the trend analysis", required = true) 
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
  @Operation(summary = "Get organization keyword analysis", description = "Get the keyword analysis from feedback for a specific organization")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Keywords retrieved successfully", 
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
    @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public ResponseEntity<List<String>> getOrganizationKeywordAnalysis(
      @Parameter(description = "ID of the organization", required = true) @PathVariable Long organizationId) {
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
  @Operation(summary = "Get organization best activities", description = "Get the best rated activities for a specific organization")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Best activities retrieved successfully", 
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = Object[].class)))),
    @ApiResponse(responseCode = "404", description = "Organization not found")
  })
  public ResponseEntity<List<Object[]>> getOrganizationBestActivities(
      @Parameter(description = "ID of the organization", required = true) @PathVariable Long organizationId,
      @Parameter(description = "Minimum number of feedbacks required", example = "3") @RequestParam(defaultValue = "3") Long minFeedbacks,
      @Parameter(description = "Pagination parameters") @PageableDefault(size = 5) Pageable pageable) {
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
  @Operation(summary = "Filter feedbacks", description = "Get filtered feedbacks based on various criteria")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Feedbacks filtered successfully", 
                content = @Content(schema = @Schema(implementation = Page.class)))
  })
  public ResponseEntity<Page<FeedbackSummaryVm>> getFilteredFeedbacks(
      @Parameter(description = "Start date for filtering") 
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
      @Parameter(description = "End date for filtering") 
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
      @Parameter(description = "Activity category for filtering") 
      @RequestParam(required = false) ActivityCategory category,
      @Parameter(description = "Activity status for filtering") 
      @RequestParam(required = false) ActivityStatus status,
      @Parameter(description = "Organization ID for filtering") 
      @RequestParam(required = false) Long organizationId,
      @Parameter(description = "Pagination parameters") 
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
  @Operation(summary = "Get feedback by ID", description = "Get detailed information about a specific feedback")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Feedback found", 
                content = @Content(schema = @Schema(implementation = FeedbackDetailVm.class))),
    @ApiResponse(responseCode = "404", description = "Feedback not found")
  })
  public ResponseEntity<FeedbackDetailVm> getFeedbackById(
      @Parameter(description = "ID of the feedback to retrieve", required = true) @PathVariable Long feedbackId) {
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
  @Operation(summary = "Respond to feedback", description = "Add organization response to a feedback")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Response added successfully", 
                content = @Content(schema = @Schema(implementation = FeedbackDetailVm.class))),
    @ApiResponse(responseCode = "400", description = "Invalid response data"),
    @ApiResponse(responseCode = "404", description = "Feedback not found")
  })
  public ResponseEntity<FeedbackDetailVm> respondToFeedback(
      @Parameter(description = "ID of the feedback to respond to", required = true) @PathVariable Long feedbackId,
      @Parameter(description = "Organization response data", required = true) @Valid @RequestBody OrganizationResponseDto responseDto) {
    FeedbackDetailVm updatedFeedback = feedbackService.addOrganizationResponse(feedbackId, responseDto);
    return ResponseEntity.ok(updatedFeedback);
  }
}
