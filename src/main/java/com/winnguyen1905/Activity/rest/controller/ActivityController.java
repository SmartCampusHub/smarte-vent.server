package com.winnguyen1905.Activity.rest.controller;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.rest.service.ActivityService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("activities")
public class ActivityController {

  private final ActivityService activityService;

  @PostMapping("/create")
  public ResponseEntity<Void> createActivity(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody ActivityDto activityDto) {
    activityService.createActivity(accountRequest, activityDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<ActivityVm> updateActivity(
      @AccountRequest TAccountRequest accountRequest,
      @RequestBody ActivityDto activityDto) {
    activityService.updateActivity(accountRequest, activityDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteActivity(
      @AccountRequest TAccountRequest accountRequest,
      @PathVariable Long id) {
    activityService.deleteActivity(accountRequest, id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ActivityVm> getActivityById(
      @AccountRequest TAccountRequest accountRequest, @PathVariable Long id) {
    ActivityVm activity = activityService.getActivityById(accountRequest, id);
    return ResponseEntity.ok(activity);
  }

  @GetMapping
  public ResponseEntity<PagedResponse<ActivityVm>> getAllActivities(Pageable pageable) {
    PagedResponse<ActivityVm> activities = activityService.getAllActivities(pageable);
    return ResponseEntity.ok(activities);
  }

  @PostMapping("/{id}/join")
  public ResponseEntity<Void> joinActivity(@AccountRequest TAccountRequest accountRequest,
      @PathVariable Long id) {
    activityService.joinActivity(accountRequest, id);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/joined")
  public ResponseEntity<PagedResponse<ActivityVm>> getJoinedActivitiess(@AccountRequest TAccountRequest accountRequest,
      Pageable pageable) {
    return ResponseEntity.ok().body(activityService.getJoinedActivities(accountRequest, pageable));
  }
}
