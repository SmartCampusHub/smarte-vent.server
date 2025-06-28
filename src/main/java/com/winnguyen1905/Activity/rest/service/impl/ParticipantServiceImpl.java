package com.winnguyen1905.activity.rest.service.impl;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.activity.model.dto.ParticipationUpdateDto;
import com.winnguyen1905.activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.persistance.repository.specification.EParticipationDetailSpecification;
import com.winnguyen1905.activity.rest.service.ParticipantService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final ParticipationDetailRepository participantRepository;

  @Override
  public void createParticipant(TAccountRequest accountRequest, ParticipationSearchParams participantDto) {
    // EAccountCredentials account = accountRepository.findById(accountRequest.getId())
    // .orElseThrow(() -> new RuntimeException("Account not found with id: " +
    // accountRequest.getId()));

    // EActivity activity = activityRepository.findById(participantDto.getActivityId())
    // .orElseThrow(() -> new RuntimeException("Activity not found with id: " +
    // participantDto.getActivityId()));

    // // Check if the participant already exists
    // if
    // (participantRepository.existsByParticipantIdAndActivityId(accountRequest.getId(),
    // participantDto.getActivityId())) {
    // throw new RuntimeException("Participant already exists for this activity");
    // }

    // EParticipationDetail participationDetail = EParticipationDetail.builder()
    // .participant(account)
    // .activity(activity)
    // .status(ParticipationStatus.UNVERIFIED)
    // .participationRole(participantDto.role())
    // .build();

    // participantRepository.save(participationDetail);
  }

  @Override
  public void updateParticipant(TAccountRequest accountRequest, ParticipationSearchParams participantDto, Long id) {
    // TODO: Implement update participant logic
  }

  @Override
  public void deleteParticipant(TAccountRequest accountRequest, Long id) {
    this.participantRepository.deleteById(id);
  }

  @Override
  public ParticipationDetailVm getParticipantById(Long id) {
    EParticipationDetail participationDetail = participantRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Participation detail not found with id: " + id));
    return ParticipationDetailVm.builder()
        .id(participationDetail.getId())
        .activityId(participationDetail.getActivity().getId())
        .activityName(participationDetail.getActivity().getActivityName())
        .activityCategory(participationDetail.getActivity().getActivityCategory())
        .activityStatus(participationDetail.getActivity().getStatus())
        .activityVenue(participationDetail.getActivity().getVenue())
        .startDate(participationDetail.getActivity().getStartDate())
        .endDate(participationDetail.getActivity().getEndDate())
        .registrationTime(participationDetail.getRegisteredAt())
        .participationRole(participationDetail.getParticipationRole())
        .participationStatus(participationDetail.getParticipationStatus())
        .processedAt(participationDetail.getProcessedAt())
        .processedBy(participationDetail.getProcessedBy())
        .rejectionReason(participationDetail.getRejectionReason())
        .verifiedNote(participationDetail.getVerifiedNote())
        .build();
  }

  // @Override
  // public List<ParticipationDetailVm> getAllParticipants() {
  // // TODO: Implement get all participants logic
  // return null;
  // }

  @Override
  public List<ParticipationDetailVm> getParticipantsByActivityId(Long activityId) {
    return null;
  }

  @Override
  public ParticipationDetailVm verifyParticipation(TAccountRequest accountRequest, ParticipationUpdateDto updateDto) {
    EParticipationDetail participationDetail = participantRepository.findById(updateDto.getParticipationId())
        .orElseThrow(
            () -> new RuntimeException("Participation detail not found with id: " + updateDto.getParticipationId()));

    Boolean isContributor = this.participantRepository.existsByParticipantIdAndActivityIdAndParticipationRole(
        accountRequest.getId(), participationDetail.getActivity().getId(), ParticipationRole.CONTRIBUTOR);

    participationDetail.setParticipationStatus(updateDto.getStatus());
    participationDetail.setProcessedAt(Instant.now());
    participationDetail.setProcessedBy(accountRequest.getUsername());
    participationDetail.setVerifiedNote(updateDto.getVerifiedNote());
    participationDetail.setRejectionReason(updateDto.getRejectionReason());
    participantRepository.save(participationDetail);

    return ParticipationDetailVm.builder()
        .id(participationDetail.getId())
        .activityId(participationDetail.getActivity().getId())
        .activityName(participationDetail.getActivity().getActivityName())
        .activityCategory(participationDetail.getActivity().getActivityCategory())
        .activityStatus(participationDetail.getActivity().getStatus())
        .activityVenue(participationDetail.getActivity().getVenue())
        .startDate(participationDetail.getActivity().getStartDate())
        .endDate(participationDetail.getActivity().getEndDate())
        .registrationTime(participationDetail.getRegisteredAt())
        .participationRole(participationDetail.getParticipationRole())
        .participationStatus(participationDetail.getParticipationStatus())
        .processedAt(participationDetail.getProcessedAt())
        .processedBy(participationDetail.getProcessedBy())
        .verifiedNote(participationDetail.getVerifiedNote())
        .build();
  }

  @Override
  public PagedResponse<ParticipationDetailVm> getParticipantDetailHistories(TAccountRequest accountRequest,
      ParticipationSearchParams participationSeachParams,
      Pageable pageable) {

    Specification<EParticipationDetail> spec = EParticipationDetailSpecification.filterBy(participationSeachParams);
    Page<EParticipationDetail> participationDetails = participantRepository.findAll(spec, pageable);

    List<ParticipationDetailVm> participationDetailVms = participationDetails.getContent().stream()
        .map(participationDetail -> ParticipationDetailVm.builder()
            .id(participationDetail.getId())
            .participationStatus(participationDetail.getParticipationStatus())
            .studentId(participationDetail.getParticipant().getId())
            .participantName(participationDetail.getParticipant().getFullName())
            .activityId(participationDetail.getActivity().getId())
            .activityName(participationDetail.getActivity().getActivityName())
            .activityCategory(participationDetail.getActivity().getActivityCategory())
            .activityStatus(participationDetail.getActivity().getStatus())
            .activityVenue(participationDetail.getActivity().getVenue())
            .startDate(participationDetail.getActivity().getStartDate())
            .endDate(participationDetail.getActivity().getEndDate())
            .participationStatus(participationDetail.getParticipationStatus())
            .registrationTime(participationDetail.getRegisteredAt())
            .identifyCode(participationDetail.getParticipant().getIdentifyCode())
            .participationRole(participationDetail.getParticipationRole())
            .processedAt(participationDetail.getProcessedAt())
            .processedBy(participationDetail.getProcessedBy())
            .rejectionReason(participationDetail.getRejectionReason())
            .verifiedNote(participationDetail.getVerifiedNote())
            .build())
        .toList();

    return PagedResponse.<ParticipationDetailVm>builder()
        .maxPageItems(pageable.getPageSize())
        .page(pageable.getPageNumber())
        .size(participationDetails.getSize())
        .results(participationDetailVms)
        .totalElements((int) participationDetails.getTotalElements())
        .totalPages(participationDetails.getTotalPages())
        .build();
  }

  @Override
  public ParticipationDetailVm rejectParticipation(TAccountRequest accountRequest,
      ParticipationUpdateDto updateDto) {
    EParticipationDetail participationDetail = participantRepository.findById(updateDto.getParticipationId())
        .orElseThrow(
            () -> new RuntimeException("Participation detail not found with id: " + updateDto.getParticipationId()));

    Boolean isContributor = this.participantRepository.existsByParticipantIdAndActivityIdAndParticipationRole(
        accountRequest.getId(), participationDetail.getActivity().getId(), ParticipationRole.CONTRIBUTOR);

    participationDetail.setParticipationStatus(updateDto.getStatus());
    participationDetail.setProcessedAt(Instant.now());
    participationDetail.setProcessedBy(accountRequest.getUsername());
    participationDetail.setRejectionReason(updateDto.getRejectionReason());
    participationDetail.setVerifiedNote(updateDto.getVerifiedNote());
    participantRepository.save(participationDetail);

    return ParticipationDetailVm.builder()
        .id(participationDetail.getId())
        .activityId(participationDetail.getActivity().getId())
        .activityName(participationDetail.getActivity().getActivityName())
        .activityCategory(participationDetail.getActivity().getActivityCategory())
        .activityStatus(participationDetail.getActivity().getStatus())
        .activityVenue(participationDetail.getActivity().getVenue())
        .startDate(participationDetail.getActivity().getStartDate())
        .endDate(participationDetail.getActivity().getEndDate())
        .registrationTime(participationDetail.getRegisteredAt())
        .participationRole(participationDetail.getParticipationRole())
        .participationStatus(participationDetail.getParticipationStatus())
        .processedAt(participationDetail.getProcessedAt())
        .processedBy(participationDetail.getProcessedBy())
        .rejectionReason(participationDetail.getRejectionReason())
        .verifiedNote(participationDetail.getVerifiedNote())
        .build();
  }
}
