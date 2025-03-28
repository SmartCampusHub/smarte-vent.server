package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;

public interface ActivityService {
  void createActivity(TAccountRequest accountRequest, ActivityDto activityDto);
  void updateActivity(TAccountRequest accountRequest, ActivityDto activityDto);
  void deleteActivity(TAccountRequest accountRequest, Long activityId);
  void getActivities(TAccountRequest accountRequest);
  PagedResponse<ActivityVm> getActivitiesByUser(TAccountRequest accountRequest);
  ActivityVm getActivityById(TAccountRequest accountRequest, Long activityId);
  PagedResponse<ActivityVm> getActivitiesByCategory(TAccountRequest accountRequest, ActivityCategory activityCategory);
}
