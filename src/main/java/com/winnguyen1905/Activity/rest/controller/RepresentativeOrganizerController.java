package com.winnguyen1905.Activity.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.RepresentativeOrganizerDto;
import com.winnguyen1905.Activity.model.viewmodel.RepresentativeOrganizerVm;
import com.winnguyen1905.Activity.rest.service.RepresentativeOrganizerService;
import lombok.RequiredArgsConstructor;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/representative-organizers")
public class RepresentativeOrganizerController {

  private final RepresentativeOrganizerService representativeOrganizerService;

  @PostMapping
  public ResponseEntity<Void> createOrganizer(@RequestAttribute TAccountRequest accountRequest,
      @RequestBody RepresentativeOrganizerDto organizerDto) {
    representativeOrganizerService.createOrganizer(accountRequest, organizerDto);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateOrganizer(@RequestAttribute TAccountRequest accountRequest,
      @RequestBody RepresentativeOrganizerDto organizerDto,
      @PathVariable Long id) {
    representativeOrganizerService.updateOrganizer(accountRequest, organizerDto, id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrganizer(@RequestAttribute TAccountRequest accountRequest,
      @PathVariable Long id) {
    representativeOrganizerService.deleteOrganizer(accountRequest, id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<RepresentativeOrganizerVm> getOrganizerById(@PathVariable Long id) {
    return ResponseEntity.ok(representativeOrganizerService.getOrganizerById(id));
  }

  @GetMapping
  public ResponseEntity<List<RepresentativeOrganizerVm>> getAllOrganizers() {
    return ResponseEntity.ok(representativeOrganizerService.getAllOrganizers());
  }
}
