package com.winnguyen1905.activity.rest.service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.ActivityScheduleDto;
import com.winnguyen1905.activity.model.viewmodel.ActivityScheduleVm;

import java.util.List;

public interface ActivityScheduleService {
  void createSchedule(TAccountRequest accountRequest, ActivityScheduleDto scheduleDto);

  void updateSchedule(TAccountRequest accountRequest, ActivityScheduleDto scheduleDto, Long id);

  void deleteSchedule(TAccountRequest accountRequest, Long id);

  ActivityScheduleVm getScheduleById(Long id);

  List<ActivityScheduleVm> getSchedulesByActivityId(Long activityId);
}
