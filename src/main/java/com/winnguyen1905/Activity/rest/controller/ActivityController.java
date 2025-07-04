package com.winnguyen1905.activity.rest.controller;

import com.winnguyen1905.activity.common.constant.SystemConstant;
import com.winnguyen1905.activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.activity.model.viewmodel.CheckJoinedActivityVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.model.viewmodel.ParticipationDetailVm;

import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.ActivityDto;
import com.winnguyen1905.activity.model.dto.ActivitySearchRequest;
import com.winnguyen1905.activity.model.dto.CheckJoinedActivityDto;
import com.winnguyen1905.activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.activity.rest.service.ActivityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("activities")
@Tag(name = "Activity Management", description = "Operations for creating, updating, and managing activities")
public class ActivityController {

  private final ActivityService activityService;

  @PostMapping("/create")
  @Operation(summary = "Create activity", description = "Create a new activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Activity created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<Void> createActivity(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Activity details", required = true) @RequestBody ActivityDto activityDto) {
    activityService.createActivity(accountRequest, activityDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/update")
  @Operation(summary = "Update activity", description = "Update an existing activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Activity updated successfully", 
                 content = @Content(schema = @Schema(implementation = ActivityVm.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<ActivityVm> updateActivity(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Updated activity details", required = true) @RequestBody ActivityDto activityDto) {
    activityService.updateActivity(accountRequest, activityDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete activity", description = "Delete an activity by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Activity deleted successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<Void> deleteActivity(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "ID of the activity to delete", required = true) @PathVariable("id") Long id) {
    activityService.deleteActivity(accountRequest, id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get activity by ID", description = "Retrieve detailed information about a specific activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Activity found", 
                 content = @Content(schema = @Schema(implementation = ActivityVm.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<ActivityVm> getActivityById(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest, 
      @Parameter(description = "ID of the activity to retrieve", required = true) @PathVariable("id") Long id) {
    ActivityVm activity = activityService.getActivityById(accountRequest, id);
    return ResponseEntity.ok(activity);
  }

  @GetMapping("/search")
  @Operation(summary = "Search activities", description = "Search for activities based on various criteria with pagination")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Search completed successfully", 
                 content = @Content(schema = @Schema(implementation = PagedResponse.class)))
  })
  public ResponseEntity<PagedResponse<ActivityVm>> getAllActivities(
      @Parameter(description = "Pagination parameters") Pageable pageable,
      @Parameter(description = "Search criteria") @ModelAttribute(SystemConstant.MODEL) ActivitySearchRequest activitySearchRequest,
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
    PagedResponse<ActivityVm> activities = activityService.getAllActivities(activitySearchRequest, pageable);
    return ResponseEntity.ok(activities);
  }

  @GetMapping("/my-contributor")
  @Operation(summary = "Get my contributor activities", description = "Get activities where the current user is a contributor")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Activities retrieved successfully", 
                 content = @Content(schema = @Schema(implementation = PagedResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<PagedResponse<ActivityVm>> getMyActivityContributors(
      @Parameter(description = "Pagination parameters") Pageable pageable,
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
    PagedResponse<ActivityVm> activities = activityService.getMyActivityContributors(accountRequest);
    return ResponseEntity.ok(activities);
  }

  @PostMapping("/join")
  @Operation(summary = "Join activity", description = "Join an existing activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Successfully joined the activity", 
                 content = @Content(schema = @Schema(implementation = ParticipationDetailVm.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input or already joined"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<ParticipationDetailVm> joinActivity(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Join activity request details", required = true) @RequestBody JoinActivityRequest joinActivityRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(activityService.joinActivity(accountRequest, joinActivityRequest));
  }

  @GetMapping("/joined")
  @Operation(summary = "Get joined activities", description = "Get activities that the current user has joined")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Activities retrieved successfully", 
                 content = @Content(schema = @Schema(implementation = PagedResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<PagedResponse<ActivityVm>> getJoinedActivitiess(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Pagination parameters") Pageable pageable) {
    return ResponseEntity.ok().body(activityService.getJoinedActivities(accountRequest, pageable));
  }

  @PostMapping("/{id}/approve")
  @Operation(summary = "Approve activity", description = "Approve an activity by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Activity approved successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<Void> approveActivity(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest, 
      @Parameter(description = "ID of the activity to approve", required = true) @PathVariable("id") Long id) {
    activityService.approveActivity(accountRequest, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/disapprove")
  @Operation(summary = "Disapprove activity", description = "Disapprove an activity by ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Activity disapproved successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<Void> disapproveActivity(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "ID of the activity to disapprove", required = true) @PathVariable("id") Long id) {
    activityService.disapproveActivity(accountRequest, id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/is-joined")
  @Operation(summary = "Check if joined activity", description = "Check if the current user has joined a specific activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Check completed", 
                 content = @Content(schema = @Schema(implementation = CheckJoinedActivityVm.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Activity not found")
  })
  public ResponseEntity<CheckJoinedActivityVm> isJoinedActivity(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Check joined activity request", required = true) @RequestBody CheckJoinedActivityDto checkJoinedActivityDto) {
    return ResponseEntity.ok(activityService.isJoinedActivity(accountRequest, checkJoinedActivityDto));
  }

  @PostMapping("/change-status")
  @Operation(summary = "Change activity status", description = "Change the status of an activity")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Status changed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public String postMethodName(
      @Parameter(description = "Status change details", required = true) @RequestBody String entity) {
    // TODO: process POST request
    return entity;
  }

  // @GetMapping("/statistical")
  // public ResponseEntity<StatisticalResponse> getStatisticalData(@AccountRequest
  // TAccountRequest accountRequest) {
  // StatisticalResponse statisticalData =
  // activityService.getStatisticalData(accountRequest);
  // return ResponseEntity.ok(statisticalData);
  // }
}
