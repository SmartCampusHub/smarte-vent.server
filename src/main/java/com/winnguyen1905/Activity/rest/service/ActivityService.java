package com.winnguyen1905.Activity.rest.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.persistance.entity.EActivity;

public interface ActivityService {
  void createActivity(TAccountRequest accountRequest, ActivityDto activityDto);
  void updateActivity(TAccountRequest accountRequest, ActivityDto activityDto);
  void deleteActivity(TAccountRequest accountRequest, Long activityId);
  PagedResponse<ActivityVm> getAllActivities(Pageable pageable);
  // PagedResponse<ActivityVm> getActivitiesByStudent(TAccountRequest accountRequest, Pageable pageable);
  // ActivityVm getActivityById(TAccountRequest accountRequest, Long activityId);
  // PagedResponse<ActivityVm> getActivitiesByCategory(TAccountRequest accountRequest, ActivityCategory activityCategory, Pageable pageable);
}
