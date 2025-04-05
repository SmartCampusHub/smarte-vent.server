package com.winnguyen1905.Activity.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.SystemConstant;
import com.winnguyen1905.Activity.model.dto.ParticipationDetailSearch;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.Activity.rest.service.ParticipantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("participants")
public class ParticipantController {

  private final ParticipantService participantService;

  @GetMapping
  public ResponseEntity<PagedResponse<ParticipationDetailVm>> getAllParticipants(
      @AccountRequest TAccountRequest accountRequest,
      @ModelAttribute(SystemConstant.MODEL) ParticipationDetailSearch searchJoinedActivities, Pageable pageable) {
    PagedResponse<ParticipationDetailVm> participants = participantService.getParticipantDetailHistories(accountRequest,
        searchJoinedActivities, pageable);
    return ResponseEntity.ok(participants);
  }

}
