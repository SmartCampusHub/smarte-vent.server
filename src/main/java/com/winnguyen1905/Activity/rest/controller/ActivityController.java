package com.winnguyen1905.Activity.rest.controller;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.model.viewmodel.ActivityViewModel;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.rest.service.ActivityService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
public class ActivityController {

  private final ActivityService activityService;

  @PostMapping("/create")
  public ResponseEntity<Void> createActivity(@AccountRequest TAccountRequest accountRequest,@RequestBody ActivityDto activityDto) {
    this.activityService.createActivity(accountRequest, activityDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{id}")
  public ResponseEntity<Void> updateActivity (@AccountRequest TAccountRequest accountRequest,@RequestBody ActivityDto activityDto,@PathVariable Long id){
    this.activityService.updateActivity(accountRequest,activityDto,id);
    return ResponseEntity.status(HttpStatus.UPGRADE_REQUIRED).build();
  }

  @DeleteMapping("/{id}")
  public String deleteActivity (TAccountRequest tAccountRequest,@PathVariable Long id) {
    activityService.deleteActivity(tAccountRequest,id);
    return "Activity has been deleted";
  }

  @GetMapping("/{id}")
  public PagedResponse<ActivityViewModel> getActivityById (@AccountRequest TAccountRequest accountRequest, @PathVariable Long id) {
    return PagedResponse.<ActivityViewModel>builder()
      .results(activityService.getActivityById(accountRequest,id))
      .build();
  }

  @GetMapping
  public PagedResponse<List<ActivityViewModel>> getAllActivities() {
    return PagedResponse.<List<ActivityViewModel>>builder()
      .results(activityService.getAllActivities())
      .build();
  }

//  @GetMapping("/category")
//  public List<ActivityViewModel>  findByCategory (ActivityCategory activityCategory) {
//    return activityService.getActivitiesByCategory(activityCategory);
//  }

}



