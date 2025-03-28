package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ParticipationDetailDto;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.Activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.Activity.rest.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {
  private final ParticipationDetailRepository participantRepository;

  @Override
  public void createParticipant(TAccountRequest accountRequest, ParticipationDetailDto participantDto) {
    // TODO: Implement create participant logic
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
    // TODO: Implement get participant by id logic
    return null;
  }

  @Override
  public List<ParticipationDetailVm> getAllParticipants() {
    // TODO: Implement get all participants logic
    return null;
  }

  @Override
  public List<ParticipationDetailVm> getParticipantsByActivityId(Long activityId) {
    // TODO: Implement get participants by activity id logic
    return null;
  }
}
