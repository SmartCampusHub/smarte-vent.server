package com.winnguyen1905.activity.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.SystemConstant;
import com.winnguyen1905.activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.activity.model.dto.ParticipationUpdateDto;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.activity.rest.service.ParticipantService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

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

  @PostMapping("/verify")
  public ResponseEntity<ParticipationDetailVm> verifyParticipation(@RequestBody ParticipationUpdateDto updateDto,
      @AccountRequest TAccountRequest accountRequest) {
    return ResponseEntity.ok(participantService.verifyParticipation(accountRequest, updateDto));
  }

  @PostMapping("/reject")
  public ResponseEntity<ParticipationDetailVm> rejectParticipation(
      @AccountRequest TAccountRequest accountRequest, @RequestBody ParticipationUpdateDto updateDto) {
    return ResponseEntity.ok(participantService.rejectParticipation(accountRequest, updateDto));
  }

  @PostMapping("/delete")
  public ResponseEntity<Void> deleteParticipation(@AccountRequest TAccountRequest accountRequest,
      @PathVariable("id") Long id) {
    this.participantService.deleteParticipant(accountRequest, id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("{id}")
  public ResponseEntity<ParticipationDetailVm> getMethodName(@PathVariable("id") Long id) {
    return ResponseEntity.ok(this.participantService.getParticipantById(id));
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
