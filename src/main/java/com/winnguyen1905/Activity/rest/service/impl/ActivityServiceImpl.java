package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.config.mapper.ActivityMapper;
import com.winnguyen1905.Activity.model.viewmodel.ActivityViewModel;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.rest.service.ActivityService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
  private final ActivityRepository activityRepository;
  private final ActivityMapper activityMapper;

  @Override
  public void createActivity(TAccountRequest accountRequest, ActivityDto activityDto) {
    // TODO Auto-generated method stub
    //validateAccountRequest (accountRequest);
    //validateActivityDTO(activityDto);
    // throw new UnsupportedOperationException("Unimplemented method 'createActivity'");
    EActivity eActivity = activityMapper.toEActivity(activityDto);
    activityRepository.save(eActivity);
  }

  @Override
  public void updateActivity(TAccountRequest accountRequest, ActivityDto activityDto, Long id) {
    EActivity eActivity = activityRepository.findById(id).orElseThrow(() ->
      new RuntimeException("Activity not found"));

    activityMapper.updateActivity(eActivity,activityDto);
    activityRepository.save(eActivity);
  }

  @Override
  public void deleteActivity(TAccountRequest accountRequest, Long activityId) {
    activityRepository.deleteById(activityId);
  }

  @Override
  public List<ActivityViewModel> getAllActivities() {
    return activityRepository.findAll().stream().map(activityMapper::toActivityViewModel).toList();
  }

  @Override
  public PagedResponse<ActivityVm> getActivitiesByUser(TAccountRequest accountRequest) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getActivitiesByUser'");
  }

  @Override
  public ActivityViewModel getActivityById(TAccountRequest accountRequest, Long activityId) {
    EActivity eActivity = activityRepository.findById(activityId).orElseThrow(() ->
      new RuntimeException("Activity not found"));
    return activityMapper.toActivityViewModel(eActivity);
  }
//  @Override
////  public List<ActivityViewModel> getActivitiesByCategory(
////      ActivityCategory activityCategory) {
////    return activityRepository.findByCategory(activityCategory).stream().map(activityMapper::toActivityViewModel).toList();
////  }
}
