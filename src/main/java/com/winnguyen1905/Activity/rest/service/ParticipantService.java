package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.Activity.model.dto.ParticipationUpdateDto;

import java.util.List;

import org.springframework.data.domain.Pageable;

public interface ParticipantService {
  
  PagedResponse<ParticipationDetailVm> getParticipantDetailHistories(TAccountRequest accountRequest,
  ParticipationSearchParams participationSeachParams, Pageable pageable);

  void createParticipant(TAccountRequest accountRequest, ParticipationSearchParams participantDto);

  void updateParticipant(TAccountRequest accountRequest, ParticipationSearchParams participantDto, Long id);

  void deleteParticipant(TAccountRequest accountRequest, Long id);

  ParticipationDetailVm getParticipantById(Long id);

  ParticipationDetailVm verifyParticipation(TAccountRequest accountRequest, ParticipationUpdateDto updateDto);

  ParticipationDetailVm rejectParticipation(TAccountRequest accountRequest, ParticipationUpdateDto updateDto);

  // List<ParticipationDetailVm> getAllParticipantsByActivityId();
  List<ParticipationDetailVm> getParticipantsByActivityId(Long activityId);
}
