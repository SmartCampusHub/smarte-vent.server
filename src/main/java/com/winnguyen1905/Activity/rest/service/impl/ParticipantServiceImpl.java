package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.model.dto.ParticipationDetailDto;
import com.winnguyen1905.Activity.model.dto.ParticipationDetailSearch;
import com.winnguyen1905.Activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.Activity.persistance.repository.specification.EParticipationDetailSpecification;
import com.winnguyen1905.Activity.rest.service.ParticipantService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final ParticipationDetailRepository participantRepository;

  @Override
  public void createParticipant(TAccountRequest accountRequest, ParticipationDetailDto participantDto) {
    // EAccountCredentials account = accountRepository.findById(accountRequest.id())
    // .orElseThrow(() -> new RuntimeException("Account not found with id: " +
    // accountRequest.id()));

    // EActivity activity = activityRepository.findById(participantDto.activityId())
    // .orElseThrow(() -> new RuntimeException("Activity not found with id: " +
    // participantDto.activityId()));

    // // Check if the participant already exists
    // if
    // (participantRepository.existsByParticipantIdAndActivityId(accountRequest.id(),
    // participantDto.activityId())) {
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
  public void updateParticipant(TAccountRequest accountRequest, ParticipationDetailDto participantDto, Long id) {
    // TODO: Implement update participant logic
  }

  @Override
  public void deleteParticipant(TAccountRequest accountRequest, Long id) {
    // TODO: Implement delete participant logic
  }

  @Override
  public ParticipationDetailVm getParticipantById(Long id) {
    return null;
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
  public void verifyParticipation(TAccountRequest accountRequest, Long participationId) {

  }

  @Override
  public PagedResponse<ParticipationDetailVm> getParticipantDetailHistories(TAccountRequest accountRequest,
      ParticipationDetailSearch pairticipationDetailSearch,
      Pageable pageable) {

    Specification<EParticipationDetail> spec = EParticipationDetailSpecification.filterBy(pairticipationDetailSearch, accountRequest);
    Page<EParticipationDetail> participationDetails = participantRepository.findAll(spec, pageable);

    List<ParticipationDetailVm> participationDetailVms = participationDetails.getContent().stream()
        .map(participationDetail -> ParticipationDetailVm.builder()
            .participationStatus(participationDetail.getParticipationStatus())
            .studentId(participationDetail.getParticipant().getId())
            .id(participationDetail.getId())
            .activityId(participationDetail.getActivity().getId())
            .activityName(participationDetail.getActivity().getActivityName())
            .activityCategory(participationDetail.getActivity().getActivityCategory())
            .activityStatus(participationDetail.getActivity().getActivityStatus())
            .activityVenue(participationDetail.getActivity().getActivityVenue())
            .startDate(participationDetail.getActivity().getStartDate())
            .endDate(participationDetail.getActivity().getEndDate())
            .registrationTime(participationDetail.getRegisteredAt())
            .participationRole(participationDetail.getParticipationRole())
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
}
