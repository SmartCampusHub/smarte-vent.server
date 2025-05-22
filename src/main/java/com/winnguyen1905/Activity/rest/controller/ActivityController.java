package com.winnguyen1905.Activity.rest.controller;

import com.winnguyen1905.Activity.common.constant.SystemConstant;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;

import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.dto.ActivitySearchRequest;
import com.winnguyen1905.Activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.Activity.rest.service.ActivityService;

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

  @GetMapping("/search")
  public ResponseEntity<PagedResponse<ActivityVm>> getAllActivities(Pageable pageable,
      @ModelAttribute(SystemConstant.MODEL) ActivitySearchRequest activitySearchRequest,
      @AccountRequest TAccountRequest accountRequest) {
    PagedResponse<ActivityVm> activities = activityService.getAllActivities(activitySearchRequest, pageable);
    return ResponseEntity.ok(activities);
  }

  @GetMapping("/my-contributor")
  public ResponseEntity<PagedResponse<ActivityVm>> getMyActivityContributors(Pageable pageable,
      @AccountRequest TAccountRequest accountRequest) {
    PagedResponse<ActivityVm> activities = activityService.getMyActivityContributors(accountRequest);
    return ResponseEntity.ok(activities);
  }

  @PostMapping("/join")
  public ResponseEntity<ParticipationDetailVm> joinActivity(@AccountRequest TAccountRequest accountRequest,
      @RequestBody JoinActivityRequest joinActivityRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(activityService.joinActivity(accountRequest, joinActivityRequest));
  }

  @GetMapping("/joined")
  public ResponseEntity<PagedResponse<ActivityVm>> getJoinedActivitiess(@AccountRequest TAccountRequest accountRequest,
      Pageable pageable) {
    return ResponseEntity.ok().body(activityService.getJoinedActivities(accountRequest, pageable));
  }

  @PostMapping("/approve")
  public String postMethodName(@PathVariable Long id) {
    return null;
  }

  // @GetMapping("/statistical")
  // public ResponseEntity<StatisticalResponse> getStatisticalData(@AccountRequest
  // TAccountRequest accountRequest) {
  // StatisticalResponse statisticalData =
  // activityService.getStatisticalData(accountRequest);
  // return ResponseEntity.ok(statisticalData);
  // }
}
