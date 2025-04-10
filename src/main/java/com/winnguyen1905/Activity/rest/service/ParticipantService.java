package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ParticipationDetailDto;
import com.winnguyen1905.Activity.model.dto.ParticipationDetailSearch;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;

import java.util.List;

import org.springframework.data.domain.Pageable;

public interface ParticipantService {
  PagedResponse<ParticipationDetailVm> getParticipantDetailHistories(TAccountRequest accountRequest,
      ParticipationDetailSearch searchJoinedActivity, Pageable pageable);

  void createParticipant(TAccountRequest accountRequest, ParticipationDetailDto participantDto);

  void updateParticipant(TAccountRequest accountRequest, ParticipationDetailDto participantDto, Long id);

  void deleteParticipant(TAccountRequest accountRequest, Long id);

  ParticipationDetailVm getParticipantById(Long id);

  ParticipationDetailVm verifyParticipation(TAccountRequest accountRequest, Long participationId);

  // List<ParticipationDetailVm> getAllParticipantsByActivityId();
  List<ParticipationDetailVm> getParticipantsByActivityId(Long activityId);
}
