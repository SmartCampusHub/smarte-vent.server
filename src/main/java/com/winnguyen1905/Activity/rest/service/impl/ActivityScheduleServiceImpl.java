package com.winnguyen1905.activity.rest.service.impl;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ScheduleStatus;
import com.winnguyen1905.activity.exception.BadRequestException;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.dto.ActivityScheduleDto;
import com.winnguyen1905.activity.model.viewmodel.ActivityScheduleVm;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EActivitySchedule;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityScheduleRepository;
import com.winnguyen1905.activity.rest.service.ActivityScheduleService;
import com.winnguyen1905.activity.rest.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityScheduleServiceImpl implements ActivityScheduleService {
  private final ActivityScheduleRepository scheduleRepository;
  private final ActivityRepository activityRepository;
  private final AuthorizationService authorizationService;

  private static final String ACTIVITY_NOT_FOUND = "Activity not found with ID: %d";
  private static final String SCHEDULE_NOT_FOUND = "Schedule not found with ID: %d";

  // -------------------- CREATE --------------------
  @Override
  @Transactional
  public void createSchedule(TAccountRequest accountRequest, ActivityScheduleDto dto) {
    validateDto(dto);
    authorizationService.validateActivityModificationAccess(dto.getActivityId(), accountRequest);

    EActivity activity = getActivity(dto.getActivityId());
    validateTiming(dto.getStartTime(), dto.getEndTime());
    checkConflict(dto.getActivityId(), dto.getStartTime(), dto.getEndTime(), null);

    EActivitySchedule schedule = EActivitySchedule.builder()
        .activity(activity)
        .startTime(dto.getStartTime())
        .endTime(dto.getEndTime())
        .activityDescription(dto.getActivityDescription())
        .location(dto.getLocation())
        .status(determineStatus(dto.getStartTime(), dto.getEndTime()))
        .createdBy(accountRequest.getUsername())
        .build();
    scheduleRepository.save(schedule);
  }

  // -------------------- UPDATE --------------------
  @Override
  @Transactional
  public void updateSchedule(TAccountRequest accountRequest, ActivityScheduleDto dto, Long id) {
    EActivitySchedule schedule = getSchedule(id);
    authorizationService.validateActivityModificationAccess(schedule.getActivity().getId(), accountRequest);
    validateTiming(dto.getStartTime(), dto.getEndTime());
    checkConflict(schedule.getActivity().getId(), dto.getStartTime(), dto.getEndTime(), id);

    if (dto.getStartTime() != null) schedule.setStartTime(dto.getStartTime());
    if (dto.getEndTime() != null) schedule.setEndTime(dto.getEndTime());
    if (StringUtils.hasText(dto.getActivityDescription())) schedule.setActivityDescription(dto.getActivityDescription());
    if (StringUtils.hasText(dto.getLocation())) schedule.setLocation(dto.getLocation());
    if (dto.getStatus() != null) schedule.setStatus(dto.getStatus());

    schedule.setUpdatedBy(accountRequest.getUsername());
    scheduleRepository.save(schedule);
  }

  // -------------------- DELETE --------------------
  @Override
  @Transactional
  public void deleteSchedule(TAccountRequest accountRequest, Long id) {
    EActivitySchedule schedule = getSchedule(id);
    authorizationService.validateActivityModificationAccess(schedule.getActivity().getId(), accountRequest);
    scheduleRepository.delete(schedule);
  }

  // -------------------- READ --------------------
  @Override
  @Transactional(readOnly = true)
  public ActivityScheduleVm getScheduleById(Long id) {
    return map(getSchedule(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ActivityScheduleVm> getSchedulesByActivityId(Long activityId) {
    getActivity(activityId); // ensure exists
    return scheduleRepository.findByActivityId(activityId).stream().map(this::map).collect(Collectors.toList());
  }

  // -------------------- Helper --------------------
  private void validateDto(ActivityScheduleDto dto) {
    if (dto == null) throw new BadRequestException("Schedule data cannot be null");
    if (dto.getActivityId() == null) throw new BadRequestException("Activity ID is required");
    if (dto.getStartTime() == null || dto.getEndTime() == null) throw new BadRequestException("Start and end time required");
    if (!StringUtils.hasText(dto.getLocation())) throw new BadRequestException("Location is required");
  }

  private void validateTiming(Instant start, Instant end) {
    if (start == null || end == null) return;
    if (!start.isBefore(end)) throw new BadRequestException("End time must be after start time");
    long minutes = ChronoUnit.MINUTES.between(start, end);
    if (minutes < 15) throw new BadRequestException("Schedule duration must be at least 15 minutes");
  }

  private void checkConflict(Long activityId, Instant start, Instant end, Long excludeId) {
    if (start == null || end == null) return;
    boolean conflict;
    if (excludeId == null)
      conflict = !scheduleRepository.checkScheduleConflicts(activityId, start, end).isEmpty();
    else
      conflict = !scheduleRepository.checkScheduleConflicts(activityId, start, end, excludeId).isEmpty();
    if (conflict) throw new BadRequestException("Schedule conflicts with existing schedule");
  }

  private ScheduleStatus determineStatus(Instant start, Instant end) {
    Instant now = Instant.now();
    if (end.isBefore(now)) return ScheduleStatus.COMPLETED;
    if (start.isAfter(now)) return ScheduleStatus.WAITING_TO_START;
    return ScheduleStatus.IN_PROGRESS;
  }

  private EActivity getActivity(Long id) {
    return activityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(ACTIVITY_NOT_FOUND, id)));
  }

  private EActivitySchedule getSchedule(Long id) {
    return scheduleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(SCHEDULE_NOT_FOUND, id)));
  }

  private ActivityScheduleVm map(EActivitySchedule s) {
    return ActivityScheduleVm.builder()
        .id(s.getId())
        .activityId(s.getActivity().getId())
        .activityName(s.getActivity().getActivityName())
        .startTime(s.getStartTime())
        .endTime(s.getEndTime())
        .activityDescription(s.getActivityDescription())
        .status(s.getStatus())
        .location(s.getLocation())
        .createdBy(s.getCreatedBy())
        .updatedBy(s.getUpdatedBy())
        .createdDate(s.getCreatedDate())
        .updatedDate(s.getUpdatedDate())
        .build();
  }
}
