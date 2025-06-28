package com.winnguyen1905.activity.rest.service;

import org.springframework.data.domain.Pageable;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.ActivityDto;
import com.winnguyen1905.activity.model.dto.ActivitySearchRequest;
import com.winnguyen1905.activity.model.dto.CheckJoinedActivityDto;
import com.winnguyen1905.activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.activity.model.viewmodel.CheckJoinedActivityVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.model.viewmodel.ParticipationDetailVm;

public interface ActivityService {
  void createActivity(TAccountRequest accountRequest, ActivityDto activityDto);
  void updateActivity(TAccountRequest accountRequest, ActivityDto activityDto);
  void deleteActivity(TAccountRequest accountRequest, Long activityId);
  PagedResponse<ActivityVm> getAllActivities(ActivitySearchRequest activitySearchRequest, Pageable pageable);
  PagedResponse<ActivityVm> getMyActivityContributors(TAccountRequest accountRequest);
  PagedResponse<ActivityVm> getJoinedActivities(TAccountRequest accountRequest, Pageable pageable);
  ParticipationDetailVm joinActivity(TAccountRequest accountRequest, JoinActivityRequest participationDetailDto);
  void approveActivity(TAccountRequest accountRequest, Long activityId);
  void disapproveActivity(TAccountRequest accountRequest, Long activityId);
  // PagedResponse<ActivityVm> getActivitiesByStudent(TAccountRequest accountRequest, Pageable pageable);
  ActivityVm getActivityById(TAccountRequest accountRequest, Long activityId);
  // PagedResponse<ActivityVm> getActivitiesByCategory(TAccountRequest accountRequest, ActivityCategory activityCategory, Pageable pageable);
  CheckJoinedActivityVm isJoinedActivity(TAccountRequest accountRequest, CheckJoinedActivityDto checkJoinedActivityDto);
}
