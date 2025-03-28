package com.winnguyen1905.Activity.rest.service;

import java.util.List;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityViewModel;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.persistance.entity.EActivity;

public interface ActivityService {
  void createActivity(TAccountRequest accountRequest, ActivityDto activityDto);
  void updateActivity(TAccountRequest accountRequest, ActivityDto activityDto,Long id);
  void deleteActivity(TAccountRequest accountRequest, Long activityId);
  List<ActivityViewModel> getAllActivities();
  PagedResponse<ActivityVm> getActivitiesByUser(TAccountRequest accountRequest);
  ActivityViewModel getActivityById(TAccountRequest accountRequest, Long activityId);
  // List<ActivityViewModel> getActivitiesByCategory(ActivityCategory activityCategory);
}
