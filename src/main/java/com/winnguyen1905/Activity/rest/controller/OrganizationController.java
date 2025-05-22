package com.winnguyen1905.Activity.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.Activity.model.dto.OrganizationDto;
import com.winnguyen1905.Activity.model.dto.OrganizationSearchRequest;
import com.winnguyen1905.Activity.model.viewmodel.OrganizationVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.rest.service.OrganizerService;
import com.winnguyen1905.Activity.common.constant.SystemConstant;
import com.winnguyen1905.Activity.model.dto.ActivityDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("organizations")
public class OrganizationController {

  private final OrganizerService organizationService;

  @GetMapping("/search")
  public ResponseEntity<PagedResponse<OrganizationVm>> getAllOrganizations(
      @ModelAttribute(SystemConstant.MODEL) OrganizationSearchRequest organizationSearchRequest, Pageable pageable) {
    return ResponseEntity.ok(organizationService.getAllOrganizers(organizationSearchRequest, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrganizationVm> getOrganization(@PathVariable Long id) {
    return ResponseEntity.ok(organizationService.getOrganizerById(id));
  }

  // @PostMapping
  // public ResponseEntity<OrganizationDto> createOrganization(@RequestBody
  // OrganizationDto organizationDto) {
  // return ResponseEntity.ok(organizationService.create(organizationDto));
  // }

  // @PutMapping("/{id}")
  // public ResponseEntity<OrganizationDto> updateOrganization(
  // @PathVariable Long id,
  // @RequestBody OrganizationDto organizationDto) {
  // organizationDto.setI(id);
  // return ResponseEntity.ok(organizationService.update(organizationDto));
  // }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
    organizationService.deleteOrganizerById(id);
    return ResponseEntity.noContent().build();
  }

  // @GetMapping("/{id}/activities")
  // public ResponseEntity<PagedResponse<ActivityDto>> getOrganizationEvents() {
  // return
  // ResponseEntity.ok(organizationService.getActivitiesByOrganizationId(id));
  // }

  // @PostMapping("/{id}/members")
  // public ResponseEntity<MemberDto> addMember(
  //     @PathVariable Long id,
  //     @RequestBody MemberDto memberDto) {
  //   return ResponseEntity.ok(organizationService.addMember(id, memberDto));
  // }

  // @DeleteMapping("/{id}/members/{memberId}")
  // public ResponseEntity<Void> removeMember(
  //     @PathVariable Long id,
  //     @PathVariable Long memberId) {
  //   organizationService.removeMember(id, memberId);
  //   return ResponseEntity.noContent().build();
  // }
}
