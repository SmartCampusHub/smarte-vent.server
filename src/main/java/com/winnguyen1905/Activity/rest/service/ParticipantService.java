package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ParticipationDetailDto;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;

import java.util.List;

public interface ParticipantService {
  void createParticipant(TAccountRequest accountRequest, ParticipationDetailDto participantDto);

  void updateParticipant(TAccountRequest accountRequest, ParticipationDetailDto participantDto, Long id);

  void deleteParticipant(TAccountRequest accountRequest, Long id);

  ParticipationDetailVm getParticipantById(Long id);

  List<ParticipationDetailVm> getAllParticipants();

  List<ParticipationDetailVm> getParticipantsByActivityId(Long activityId);
}
