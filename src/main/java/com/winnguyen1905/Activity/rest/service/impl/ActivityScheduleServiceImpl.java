package com.winnguyen1905.activity.rest.service.impl;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.ActivityScheduleDto;
import com.winnguyen1905.activity.model.viewmodel.ActivityScheduleVm;
import com.winnguyen1905.activity.persistance.repository.ActivityScheduleRepository;
import com.winnguyen1905.activity.rest.service.ActivityScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityScheduleServiceImpl implements ActivityScheduleService {
  private final ActivityScheduleRepository scheduleRepository;

  @Override
  public void createSchedule(TAccountRequest accountRequest, ActivityScheduleDto scheduleDto) {
    // TODO: Implement create schedule logic
  }

  @Override
  public void updateSchedule(TAccountRequest accountRequest, ActivityScheduleDto scheduleDto, Long id) {
    // TODO: Implement update schedule logic
  }

  @Override
  public void deleteSchedule(TAccountRequest accountRequest, Long id) {
    // TODO: Implement delete schedule logic
  }

  @Override
  public ActivityScheduleVm getScheduleById(Long id) {
    // TODO: Implement get schedule by id logic
    return null;
  }

  @Override
  public List<ActivityScheduleVm> getSchedulesByActivityId(Long activityId) {
    // TODO: Implement get schedules by activity id logic
    return null;
  }
}
