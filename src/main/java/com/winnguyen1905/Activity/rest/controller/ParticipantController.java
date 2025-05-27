package com.winnguyen1905.Activity.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.SystemConstant;
import com.winnguyen1905.Activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.Activity.rest.service.ParticipantService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("participants")
public class ParticipantController {

  private final ParticipantService participantService;

  @GetMapping("")
  public ResponseEntity<PagedResponse<ParticipationDetailVm>> getAllParticipants(
      @AccountRequest TAccountRequest accountRequest, Pageable pageable,
      @ModelAttribute(SystemConstant.MODEL) ParticipationSearchParams participationSeachParams) {
    PagedResponse<ParticipationDetailVm> participants = participantService.getParticipantDetailHistories(accountRequest,
        participationSeachParams, pageable);
    return ResponseEntity.ok(participants);
  }

  @PostMapping("/{id}/verify")
  public ResponseEntity<ParticipationDetailVm> postMethodName(@PathVariable("id") Long id,
      @AccountRequest TAccountRequest accountRequest) {
    return ResponseEntity.ok(participantService.verifyParticipation(accountRequest, id));
  }

  @PostMapping("/{id}/delete")
  public ResponseEntity<Void> postMethodName(@AccountRequest TAccountRequest accountRequest, @PathVariable("id") Long id) {
    this.participantService.deleteParticipant(accountRequest, id);
    return ResponseEntity.noContent().build();
  }

  // @GetMapping("/admin")
  // public ResponseEntity<PagedResponse<ParticipationDetailVm>>
  // getParticipantsByAdmin(
  // @AccountRequest TAccountRequest accountRequest,
  // @ModelAttribute(SystemConstant.MODEL) ParticipationSearchParams
  // participationSeachParams, Pageable pageable) {
  // PagedResponse<ParticipationDetailVm> participants =
  // participantService.getParticipantDetailHistories(accountRequest,
  // participationSeachParams, pageable);
  // return ResponseEntity.ok(participants);
  // }
}
