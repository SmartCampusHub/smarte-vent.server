package com.winnguyen1905.Activity.rest.service.impl;

import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.rest.service.ActivityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

  private final ActivityRepository activityRepository;

  @Override
  public void createActivity(TAccountRequest accountRequest, ActivityDto activityDto) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createActivity'");
  }

  @Override
  public void updateActivity(TAccountRequest accountRequest, ActivityDto activityDto) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'updateActivity'");
  }

  @Override
  public void deleteActivity(TAccountRequest accountRequest, Long activityId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteActivity'");
  }

  @Override
  public void getActivities(TAccountRequest accountRequest) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getActivities'");
  }

  @Override
  public PagedResponse<ActivityVm> getActivitiesByUser(TAccountRequest accountRequest) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getActivitiesByUser'");
  }

  @Override
  public ActivityVm getActivityById(TAccountRequest accountRequest, Long activityId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getActivityById'");
  }

  @Override
  public PagedResponse<ActivityVm> getActivitiesByCategory(TAccountRequest accountRequest,
      ActivityCategory activityCategory) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getActivitiesByCategory'");
  }

}
