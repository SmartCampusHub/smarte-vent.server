package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EActivity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.exception.BadRequestException;
import com.winnguyen1905.Activity.exception.ResourceAlreadyExistsException;
import com.winnguyen1905.Activity.model.dto.ActivityDto;
import com.winnguyen1905.Activity.model.dto.ActivityScheduleDto;
import com.winnguyen1905.Activity.model.dto.ActivitySearchRequest;
import com.winnguyen1905.Activity.model.dto.CheckJoinedActivityDto;
import com.winnguyen1905.Activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.Activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.Activity.model.viewmodel.ActivityScheduleVm;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.CheckJoinedActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.OrganizationVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityScheduleRepository;
import com.winnguyen1905.Activity.persistance.repository.OrganizationRepository;
import com.winnguyen1905.Activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.Activity.persistance.repository.specification.EActivitySpecification;
import com.winnguyen1905.Activity.rest.service.ActivityService;
import com.winnguyen1905.Activity.rest.service.EmailService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import java.util.stream.Collectors;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;

import com.winnguyen1905.Activity.persistance.entity.EActivitySchedule;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.Activity.model.viewmodel.FeedbackDetailVm;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

  private final EmailService emailService;
  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final OrganizationRepository organizationRepository;
  private final ActivityScheduleRepository activityScheduleRepository;
  private final ParticipationDetailRepository participationDetailRepository;

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
        .venue(activityDto.getActivityVenue())
        .capacityLimit(0) // Changed from capacity to capacityLimit
        .capacityLimit(activityDto.getCapacityLimit())
        .activityCategory(activityDto.getActivityCategory())
        .description(activityDto.getActivityDescription())
        .startDate(activityDto.getStartDate())
        .endDate(activityDto.getEndDate())
        .status(ActivityStatus.PENDING) // Set to null initially
        .imageUrl(activityDto.getImageUrl())
        .shortDescription(activityDto.getShortDescription())
        .tags(activityDto.getTags())
        .currentParticipants(0)
        .address(activityDto.getAddress())
        .latitude(activityDto.getLatitude())
        .longitude(activityDto.getLongitude())
        .fee(activityDto.getFee())
        .isFeatured(false)
        .isApproved(false)
        .likes(0)
        .registrationDeadline(activityDto.getRegistrationDeadline())
        .activityCategory(activityDto.getActivityCategory())
        .description(activityDto.getActivityDescription())
        .organization(this.organizationRepository.findById(accountRequest.id())
            .orElseThrow(() -> new EntityNotFoundException("Not found organization")))
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
  @Transactional
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
    existingActivity.setVenue(activityDto.getActivityVenue());
    existingActivity.setCapacityLimit(activityDto.getCapacityLimit()); // Changed from capacity to capacityLimit
    // existingActivity.setStatus(activityDto.getActivityStatus());
    existingActivity.setActivityCategory(activityDto.getActivityCategory());
    existingActivity.setDescription(activityDto.getActivityDescription());
    existingActivity.setAttendanceScoreUnit(activityDto.getAttendanceScoreUnit());
    existingActivity.setUpdatedById(accountRequest.id());
    existingActivity.setUpdatedDate(Instant.now());
    existingActivity.setShortDescription(activityDto.getShortDescription());
    existingActivity.setTags(activityDto.getTags());
    existingActivity.setCurrentParticipants(activityDto.getCurrentParticipants());
    existingActivity.setAddress(activityDto.getAddress());
    existingActivity.setLatitude(activityDto.getLatitude());
    existingActivity.setLongitude(activityDto.getLongitude());
    existingActivity.setFee(activityDto.getFee());
    existingActivity.setRegistrationDeadline(activityDto.getRegistrationDeadline());
    existingActivity.setImageUrl(activityDto.getImageUrl());
    existingActivity.setIsFeatured(activityDto.getIsFeatured());
    existingActivity.setIsApproved(activityDto.getIsApproved());
    existingActivity.setLikes(activityDto.getLikes());
    existingActivity.setActivityCategory(activityDto.getActivityCategory());
    existingActivity.setDescription(activityDto.getActivityDescription());

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

    if (activity.getStatus() == ActivityStatus.CANCELLED) {
      throw new BadRequestException("Activity is already cancelled");
    }
    if (activity.getStatus() == ActivityStatus.COMPLETED) {
      throw new BadRequestException("Activity is already completed");
    }
  }

  @Override
  public PagedResponse<ActivityVm> getAllActivities(ActivitySearchRequest activitySearchRequest, Pageable pageable) {

    Specification<EActivity> activitySpecification = EActivitySpecification.filterBy(activitySearchRequest);

    Page<EActivity> activities = activityRepository.findAll(activitySpecification, pageable);

    List<ActivityVm> activityVms = activities.getContent().stream()
        .map(activity -> ActivityVm.builder()
            .id(activity.getId())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate())
            .activityName(activity.getActivityName())
            .description(activity.getDescription())
            .createdDate(activity.getCreatedDate())

            .activityVenue(activity.getVenue())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate())
            .capacityLimit(activity.getCapacityLimit())
            .activityStatus(activity.getStatus()) // Updated to use getStatus()
            .activityCategory(activity.getActivityCategory())
            .tags(activity.getTags())
            .currentParticipants(activity.getCurrentParticipants())
            .address(activity.getAddress())
            .latitude(activity.getLatitude())
            .longitude(activity.getLongitude())
            .fee(activity.getFee())
            .isFeatured(activity.getIsFeatured())
            .isApproved(activity.getIsApproved())
            .organization(OrganizationVm.builder()
                .id(activity.getOrganization().getId())
                .organizationName(activity.getOrganization().getName())
                .representativeEmail(activity.getOrganization().getEmail())
                .representativePhone(activity.getOrganization().getPhone())
                .build())
            .likes(activity.getLikes())
            .registrationDeadline(activity.getRegistrationDeadline())
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
  // @Override
  // public PagedResponse<ActivityVm> getActivitiesByCategory(TAccountRequest
  // accountRequest,
  // ActivityCategory activityCategory) {
  // validateAccountRequest(accountRequest);
  //
  // List<EActivity> activities =
  // activityRepository.findByCategory(activityCategory);
  // List<ActivityVm> activityVms = activities.stream()
  // .map(activityMapper::toViewModel)
  // .toList();
  //
  // return PagedResponse.<ActivityVm>builder()
  // .maxPageItems(10)
  // .page(1)
  // .size(activityVms.size())
  // .results(activityVms)
  // .totalElements(activityVms.size())
  // .totalPages(1)
  // .build();
  // }

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
    if (activityDto.getCapacityLimit() != null && activityDto.getCapacityLimit() < 1) { // Changed from capacity to
                                                                                        // capacityLimit
      throw new BadRequestException("Capacity limit must be greater than 0");
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

  @Override
  public ActivityVm getActivityById(TAccountRequest accountRequest, Long activityId) {
    EActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> new EntityNotFoundException("Not found activity"));

    List<ActivityScheduleVm> activitySchedules = activity.getActivitySchedules().stream()
        .map(schedule -> ActivityScheduleVm.builder()
            .id(schedule.getId())
            .activityId(activity.getId())
            .activityName(activity.getActivityName())
            .startTime(schedule.getStartTime())
            .endTime(schedule.getEndTime())
            .activityDescription(schedule.getActivityDescription())
            .status(schedule.getStatus())
            .location(schedule.getLocation())
            .createdBy(schedule.getCreatedBy())
            .updatedBy(schedule.getUpdatedBy())
            .createdDate(schedule.getCreatedDate())
            .updatedDate(schedule.getUpdatedDate())
            .build())
        .collect(Collectors.toList());

    // Map feedbacks to FeedbackDetailVm
    List<FeedbackDetailVm> feedbacks = activity.getFeedbacks().stream()
        .map(feedback -> FeedbackDetailVm.builder()
            .id(feedback.getId())
            .activityId(activity.getId())
            .activityName(activity.getActivityName())
            .studentId(feedback.getParticipation().getParticipant().getId())
            .studentName(feedback.getParticipation().getParticipant().getFullName())
            .rating(feedback.getRating())
            .feedbackDescription(feedback.getFeedbackDescription())
            .createdDate(feedback.getCreatedAt())
            .participationId(feedback.getParticipation().getId())
            .organizationResponse(feedback.getOrganizationResponse())
            .respondedAt(feedback.getRespondedAt())
            .hasResponse(feedback.getOrganizationResponse() != null && !feedback.getOrganizationResponse().isEmpty())
            .build())
        .collect(Collectors.toList());

    return ActivityVm.builder().id(activity.getId())
        .activitySchedules(activitySchedules)
        .startDate(activity.getStartDate())
        .endDate(activity.getEndDate())
        .createdDate(activity.getCreatedDate())
        .activityName(activity.getActivityName())
        .description(activity.getDescription())
        .activityVenue(activity.getVenue())
        .startDate(activity.getStartDate())
        .endDate(activity.getEndDate())
        .organization(OrganizationVm.builder()
            .id(activity.getOrganization().getId())
            .organizationName(activity.getOrganization().getName())
            .representativeEmail(activity.getOrganization().getEmail())
            .representativePhone(activity.getOrganization().getPhone())
            .build())
        .capacityLimit(activity.getCapacityLimit())
        .activityStatus(activity.getStatus())
        .activityCategory(activity.getActivityCategory())
        .tags(activity.getTags())
        .currentParticipants(activity.getCurrentParticipants())
        .address(activity.getAddress())
        .latitude(activity.getLatitude())
        .longitude(activity.getLongitude())
        .fee(activity.getFee())
        .isFeatured(activity.getIsFeatured())
        .isApproved(activity.getIsApproved())
        .likes(activity.getLikes())
        .registrationDeadline(activity.getRegistrationDeadline())
        .createdDate(activity.getCreatedDate())
        .feedbacks(feedbacks) // Add feedbacks to the response
        .build();
  }

  @Override
  @Transactional
  public ParticipationDetailVm joinActivity(TAccountRequest accountRequest,
      JoinActivityRequest joinActivityRequest) {
    EActivity activity = activityRepository.findById(joinActivityRequest.activityId())
        .orElseThrow(() -> new EntityNotFoundException("Not found activity"));

    EAccountCredentials account = this.accountRepository.findById(accountRequest.id())
        .orElseThrow(() -> new EntityNotFoundException("Not found account request"));
    EParticipationDetail participationDetails = participationDetailRepository.findByStudentIdAndActivityId(
        accountRequest.id(),
        joinActivityRequest.activityId()).orElse(null);
    Boolean x = participationDetailRepository.existsByParticipantIdAndActivityId(account.getId(),
        joinActivityRequest.activityId());

    if (x)
      throw new ResourceAlreadyExistsException("You have already joined this activity");

    if (activity.getCurrentParticipants() == activity.getCapacityLimit())
      throw new BadRequestException("Out of slot");

    EParticipationDetail participationDetail = EParticipationDetail.builder()
        .participant(account)
        .activity(activity)
        .participationStatus(ParticipationStatus.UNVERIFIED)
        .participationRole(joinActivityRequest.role())
        .registeredAt(Instant.now())
        .build();

    activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
    activityRepository.save(activity);
    EParticipationDetail savedParticipationDetail = participationDetailRepository.save(participationDetail);

    // try {
    // emailService.sendEmail(
    // account.getEmail(),
    // activity.getActivityName(),
    // "You have successfully joined as a " + participationDetailDto.role());
    // } catch (Exception e) {
    // System.err.println("Failed to send email to " + account.getEmail() + ": " +
    // e.getMessage());
    // }
    return ParticipationDetailVm.builder()
        .id(savedParticipationDetail.getId())
        .studentId(accountRequest.id())
        .activityId(savedParticipationDetail.getActivity().getId())
        .activityName(savedParticipationDetail.getActivity().getActivityName())
        .participationStatus(savedParticipationDetail.getParticipationStatus())
        .activityCategory(savedParticipationDetail.getActivity().getActivityCategory())
        .activityStatus(savedParticipationDetail.getActivity().getStatus())
        .activityVenue(savedParticipationDetail.getActivity().getVenue())
        .startDate(savedParticipationDetail.getActivity().getStartDate())
        .endDate(savedParticipationDetail.getActivity().getEndDate())
        .registrationTime(savedParticipationDetail.getRegisteredAt())
        .participationRole(savedParticipationDetail.getParticipationRole())
        .build();
  }

  @Override
  public PagedResponse<ActivityVm> getJoinedActivities(TAccountRequest accountRequest, Pageable pageable) {

    List<EParticipationDetail> particiList = participationDetailRepository.findAllByParticipantId(accountRequest.id());
    List<Long> ids = particiList.stream().map(EParticipationDetail::getActivity).map(EActivity::getId).toList();
    Page<EActivity> activityPage = activityRepository.findAllByIds(ids, pageable);

    List<ActivityVm> activityVms = activityPage.getContent().stream()
        .map(activity -> ActivityVm.builder()
            .id(activity.getId())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate()).organization(OrganizationVm.builder()
                .id(activity.getOrganization().getId())
                .organizationName(activity.getOrganization().getName())
                .representativeEmail(activity.getOrganization().getEmail())
                .representativePhone(activity.getOrganization().getPhone())
                .build())
            .createdDate(activity.getCreatedDate())
            .activityName(activity.getActivityName())
            .description(activity.getDescription())
            .activityVenue(activity.getVenue())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate())
            .capacityLimit(activity.getCapacityLimit())
            .activityStatus(activity.getStatus()) // Updated to use getStatus()
            .activityCategory(activity.getActivityCategory())
            .tags(activity.getTags())
            .currentParticipants(activity.getCurrentParticipants())
            .address(activity.getAddress())
            .latitude(activity.getLatitude())
            .longitude(activity.getLongitude())
            .fee(activity.getFee())
            .isFeatured(activity.getIsFeatured())
            .isApproved(activity.getIsApproved())
            .likes(activity.getLikes())
            .registrationDeadline(activity.getRegistrationDeadline())
            .build())
        .collect(Collectors.toList());

    return PagedResponse.<ActivityVm>builder()
        .maxPageItems(pageable.getPageSize())
        .page(pageable.getPageNumber())
        .size(activityVms.size())
        .results(activityVms)
        .totalElements((int) activityPage.getTotalElements())
        .totalPages(activityPage.getTotalPages())
        .build();
  }

  @Override
  public PagedResponse<ActivityVm> getMyActivityContributors(TAccountRequest accountRequest) {
    List<EParticipationDetail> participationDetails = participationDetailRepository
        .findAllByParticipantIdAndParticipationRole(accountRequest.id(), ParticipationRole.CONTRIBUTOR);

    List<ActivityVm> activityVms = participationDetails.stream().map(EParticipationDetail::getActivity).toList()
        .stream()
        .filter(activity -> activity.getIsApproved() == true)
        .map(activity -> ActivityVm.builder().id(activity.getId())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate()).organization(OrganizationVm.builder()
                .id(activity.getOrganization().getId())
                .organizationName(activity.getOrganization().getName())
                .representativeEmail(activity.getOrganization().getEmail())
                .representativePhone(activity.getOrganization().getPhone())
                .build())
            .organization(null)
            .activityName(activity.getActivityName())
            .description(activity.getDescription())
            .createdDate(activity.getCreatedDate())
            .activityVenue(activity.getVenue())
            .startDate(activity.getStartDate())
            .endDate(activity.getEndDate())
            .capacityLimit(activity.getCapacityLimit())
            .activityStatus(activity.getStatus()) // Updated to use getStatus()
            .activityCategory(activity.getActivityCategory())
            .tags(activity.getTags())
            .currentParticipants(activity.getCurrentParticipants())
            .address(activity.getAddress())
            .latitude(activity.getLatitude())
            .longitude(activity.getLongitude())
            .fee(activity.getFee())
            .isFeatured(activity.getIsFeatured())
            .isApproved(activity.getIsApproved())
            .likes(activity.getLikes())
            .registrationDeadline(activity.getRegistrationDeadline())
            .build())
        .sorted(Comparator.comparing(ActivityVm::getStartDate)) // Sort by startDate
        .collect(Collectors.toList());

    return PagedResponse.<ActivityVm>builder()
        .maxPageItems(activityVms.size())
        .page(3)
        .size(activityVms.size())
        .results(activityVms)
        .totalElements((int) activityVms.size())
        .totalPages(3)
        .build();
  }

  @Override
  public void approveActivity(TAccountRequest accountRequest, Long activityId) {
    EActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> new EntityNotFoundException("Not found activity"));
    if (activity.getIsApproved() == true) {
      throw new BadRequestException("Activity is already approved");
    }
    activity.setIsApproved(true);
    activity.setStatus(ActivityStatus.PUBLISHED);
    activityRepository.save(activity);

  }

  @Override
  public void disapproveActivity(TAccountRequest accountRequest, Long activityId) {
    EActivity activity = activityRepository.findById(activityId)
        .orElseThrow(() -> new EntityNotFoundException("Not found activity"));
    if (activity.getIsApproved() == false) {
      throw new BadRequestException("Activity is already disapproved");
    }
    activity.setIsApproved(false);
    activity.setStatus(ActivityStatus.PENDING);
    activityRepository.save(activity);

  }

  @Override
  public CheckJoinedActivityVm isJoinedActivity(TAccountRequest accountRequest,
      CheckJoinedActivityDto checkJoinedActivityDto) {
    // EActivity activity =
    // activityRepository.findById(checkJoinedActivityDto.activityId())
    // .orElseThrow(() -> new EntityNotFoundException("Not found activity"));
    // Boolean isJoined = ;
    EParticipationDetail participationDetail = participationDetailRepository.findByStudentIdAndActivityId(
        accountRequest.id(),
        checkJoinedActivityDto.activityId()).orElse(null);
    if (participationDetail == null) {
      return CheckJoinedActivityVm.builder()
          .isJoined(false)
          .registeredAt(null)
          .processedAt(null)
          .processedBy(null)
          .rejectionReason(null)
          .verifiedNote(null)
          .status(null)
          .role(null)
          .build();
    }
    
    return CheckJoinedActivityVm.builder()
        .isJoined(true)
        .registeredAt(participationDetail.getRegisteredAt())
        .processedAt(participationDetail.getProcessedAt())
        .processedBy(participationDetail.getProcessedBy())
        .rejectionReason(participationDetail.getRejectionReason())
        .verifiedNote(participationDetail.getVerifiedNote())
        .status(participationDetail.getParticipationStatus())
        .role(participationDetail.getParticipationRole())
        .build();
  }
}
