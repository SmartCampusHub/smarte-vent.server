package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.persistance.entity.EActivity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.exception.BadRequestException;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.dto.ActivityScheduleDto;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityScheduleRepository;
import com.winnguyen1905.Activity.rest.service.ActivityService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import java.util.stream.Collectors;
import java.util.ArrayList;
import com.winnguyen1905.Activity.persistance.entity.EActivitySchedule;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

  private final ActivityScheduleRepository activityScheduleRepository;
  private final ActivityRepository activityRepository;

  @Override
  public void createActivity(TAccountRequest accountRequest, ActivityDto activityDto) {
    validateActivityDto(activityDto);
    validateAccountRequest(accountRequest);

    // Create and save activity using builder
    EActivity activity = EActivity.builder()
        .activityName(activityDto.getActivityName())
        .description(activityDto.getDescription())
        .startDate(activityDto.getStartDate())
        .endDate(activityDto.getEndDate())
        .activityVenue(activityDto.getActivityVenue())
        .capacity(activityDto.getCapacity())
        .activityStatus(activityDto.getActivityStatus())
        .activityCategory(activityDto.getActivityCategory())
        .description(activityDto.getActivityDescription())
        // .activityImage(activityDto.getActivityImage())
        // .activityLink(activityDto.getActivityLink())
        .attendanceScoreUnit(activityDto.getAttendanceScoreUnit())
        .createdById(accountRequest.id())
        .build();

    activity = activityRepository.save(activity);

    // craete and save activity schedules
    if (activityDto.getActivitySchedules() != null && !activityDto.getActivitySchedules().isEmpty()) {
      List<EActivitySchedule> schedules = new ArrayList<>();

      for (ActivityScheduleDto scheduleDto : activityDto.getActivitySchedules()) {
        EActivitySchedule schedule = EActivitySchedule.builder()
            .activity(activity)
            .startTime(scheduleDto.getStartTime())
            .endTime(scheduleDto.getEndTime())
            .activityDescription(scheduleDto.getActivityDescription())
            .status(scheduleDto.getStatus())
            .location(scheduleDto.getLocation())
            .createdBy(accountRequest.username())
            .build();
        schedules.add(schedule);
      }

      activityScheduleRepository.saveAll(schedules);
    }
  }

  @Override
  public void updateActivity(TAccountRequest accountRequest, ActivityDto activityDto) {
    validateActivityDto(activityDto);
    validateAccountRequest(accountRequest);

    EActivity existingActivity = activityRepository.findById(activityDto.getId())
        .orElseThrow(() -> new RuntimeException("Activity not found"));

    // Update activity fields
    existingActivity.setActivityName(activityDto.getActivityName());
    existingActivity.setDescription(activityDto.getDescription());
    existingActivity.setStartDate(activityDto.getStartDate());
    existingActivity.setEndDate(activityDto.getEndDate());
    existingActivity.setActivityVenue(activityDto.getActivityVenue());
    existingActivity.setCapacity(activityDto.getCapacity());
    existingActivity.setActivityStatus(activityDto.getActivityStatus());
    existingActivity.setActivityCategory(activityDto.getActivityCategory());
    existingActivity.setDescription(activityDto.getActivityDescription());
    existingActivity.setAttendanceScoreUnit(activityDto.getAttendanceScoreUnit());
    existingActivity.setUpdatedById(accountRequest.id());

    activityRepository.save(existingActivity);

    // Update schedules if provided
    if (activityDto.getActivitySchedules() != null && !activityDto.getActivitySchedules().isEmpty()) {
      // Delete existing schedules
      activityScheduleRepository.deleteByActivityId(activityDto.getId());

      // Create new schedules
      List<EActivitySchedule> schedules = activityDto.getActivitySchedules().stream()
          .map(scheduleDto -> EActivitySchedule.builder()
              .activity(existingActivity)
              .startTime(scheduleDto.getStartTime())
              .endTime(scheduleDto.getEndTime())
              .activityDescription(scheduleDto.getActivityDescription())
              .status(scheduleDto.getStatus())
              .location(scheduleDto.getLocation())
              .createdBy(accountRequest.username())
              .build())
          .collect(Collectors.toList());

      activityScheduleRepository.saveAll(schedules);
    }
  }

  @Override
  public void deleteActivity(TAccountRequest accountRequest, Long activityId) {
    validateDeleteRequest(accountRequest, activityId);
    activityRepository.deleteById(activityId);
  }

  private void validateDeleteRequest(TAccountRequest accountRequest, Long activityId) {
    EActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> new RuntimeException("Activity not found"));

    if (activity == null) {
      throw new BadRequestException("Activity not found");
    }

    if (activity.getActivityStatus() == ActivityStatus.CANCELLED) {
      throw new BadRequestException("Activity is already cancelled");
    }
    if (activity.getActivityStatus() == ActivityStatus.FINISHED) {
      throw new BadRequestException("Activity is already completed");
    }
    if (activity.getRepresentativeOrganizer().getId() != accountRequest.id()) {
      throw new BadRequestException("No authorization to delete this activity");
    }
  }

  @Override
  public PagedResponse<ActivityVm> getAllActivities(Pageable pageable) {
    Page<EActivity> activities = activityRepository.findAll(pageable);

    List<ActivityVm> activityVms = activities.getContent().stream()
        .map(activity -> ActivityVm.builder()
            .id(activity.getId())
            .activityName(activity.getActivityName())
            .description(activity.getDescription())
            .activityVenue(activity.getActivityVenue())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate())
            .capacity(activity.getCapacity())
            .activityStatus(activity.getActivityStatus())
            .activityCategory(activity.getActivityCategory())
            .build())
        .collect(Collectors.toList());

    return PagedResponse.<ActivityVm>builder()
        .maxPageItems(pageable.getPageSize())
        .page(pageable.getPageNumber())
        .size(activityVms.size())
        .results(activityVms)
        .totalElements((int) activities.getTotalElements())
        .totalPages(activities.getTotalPages())
        .build();
  }

  // @Override
  // public PagedResponse<ActivityVm> getActivitiesByStudent(TAccountRequest
  // accountRequest, Pageable pageable) {
  // validateAccountRequest(accountRequest);

  // List<EActivity> activities =
  // activityRepository.findByUserId(accountRequest.id());
  // List<ActivityVm> activityVms = activities.stream()
  // .map(activityMapper::toViewModel)
  // .toList();

  // return PagedResponse.<ActivityVm>builder()
  // .maxPageItems(10)
  // .page(1)
  // .size(activityVms.size())
  // .results(activityVms)
  // .totalElements(activityVms.size())
  // .totalPages(1)
  // .build();
  // }

  // @Override
  // public ActivityVm getActivityById(TAccountRequest accountRequest, Long
  // activityId) {
  // validateAccountRequest(accountRequest);

  // EActivity activity = activityRepository.findById(activityId)
  // .orElseThrow(() -> new RuntimeException("Activity not found"));

  // return activityMapper.toViewModel(activity);
  // }
//
//   @Override
//   public PagedResponse<ActivityVm> getActivitiesByCategory(TAccountRequest
//   accountRequest,
//   ActivityCategory activityCategory) {
//   validateAccountRequest(accountRequest);
//
//   List<EActivity> activities =
//   activityRepository.findByCategory(activityCategory);
//   List<ActivityVm> activityVms = activities.stream()
//   .map(activityMapper::toViewModel)
//   .toList();
//
//   return PagedResponse.<ActivityVm>builder()
//   .maxPageItems(10)
//   .page(1)
//   .size(activityVms.size())
//   .results(activityVms)
//   .totalElements(activityVms.size())
//   .totalPages(1)
//   .build();
//   }

  private void validateActivityDto(ActivityDto activityDto) {
    if (activityDto == null) {
      throw new BadRequestException("Activity data cannot be null");
    }
    if (activityDto.getActivityName() == null || activityDto.getActivityName().trim().isEmpty()) {
      throw new BadRequestException("Activity name is required");
    }
    if (activityDto.getStartDate() == null) {
      throw new BadRequestException("Start date is required");
    }
    if (activityDto.getEndDate() == null) {
      throw new BadRequestException("End date is required");
    }
    if (activityDto.getStartDate().isAfter(activityDto.getEndDate())) {
      throw new BadRequestException("Start date must be before end date");
    }
    if (activityDto.getCapacity() != null && activityDto.getCapacity() < 1) {
      throw new BadRequestException("Capacity must be greater than 0");
    }
  }

  private void validateAccountRequest(TAccountRequest accountRequest) {
    if (accountRequest == null) {
      throw new BadRequestException("Account request cannot be null");
    }
    if (accountRequest.id() == null) {
      throw new BadRequestException("Account ID is required");
    }
    if (accountRequest.username() == null || accountRequest.username().trim().isEmpty()) {
      throw new BadRequestException("Username is required");
    }
    if (accountRequest.role() == null) {
      throw new BadRequestException("Account role is required");
    }
  }
}
