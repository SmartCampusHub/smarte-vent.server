package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.RepresentativeOrganizerDto;
import com.winnguyen1905.Activity.model.viewmodel.RepresentativeOrganizerVm;
import com.winnguyen1905.Activity.persistance.entity.ERepresentativeOrganizer;
import com.winnguyen1905.Activity.persistance.repository.RepresentativeOrganizerRepository;
import com.winnguyen1905.Activity.rest.service.RepresentativeOrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepresentativeOrganizerServiceImpl implements RepresentativeOrganizerService {

  private final RepresentativeOrganizerRepository organizerRepository;

  @Override
  public void createOrganizer(TAccountRequest accountRequest, RepresentativeOrganizerDto organizerDto) {
    ERepresentativeOrganizer organizer = ERepresentativeOrganizer.builder()
        .organizationName(organizerDto.organizationName())
        .representativeName(organizerDto.representativeName())
        .representativeEmail(organizerDto.representativeEmail())
        .representativePhone(organizerDto.representativePhone())
        .representativePosition(organizerDto.representativePosition())
        .build();

    organizerRepository.save(organizer);
  }

  @Override
  public void updateOrganizer(TAccountRequest accountRequest, RepresentativeOrganizerDto organizerDto, Long id) {
    ERepresentativeOrganizer organizer = organizerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));

    organizer.setOrganizationName(organizerDto.organizationName());
    organizer.setRepresentativeName(organizerDto.representativeName());
    organizer.setRepresentativeEmail(organizerDto.representativeEmail());
    organizer.setRepresentativePhone(organizerDto.representativePhone());
    organizer.setRepresentativePosition(organizerDto.representativePosition());

    organizerRepository.save(organizer);
  }

  @Override
  public void deleteOrganizer(TAccountRequest accountRequest, Long id) {
    if (!organizerRepository.existsById(id)) {
      throw new RuntimeException("Organizer not found with id: " + id);
    }
    organizerRepository.deleteById(id);
  }

  @Override
  public RepresentativeOrganizerVm getOrganizerById(Long id) {
    ERepresentativeOrganizer organizer = organizerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));

    return mapToViewModel(organizer);
  }

  @Override
  public List<RepresentativeOrganizerVm> getAllOrganizers() {
    return organizerRepository.findAll().stream()
        .map(this::mapToViewModel)
        .toList();
  }

  private RepresentativeOrganizerVm mapToViewModel(ERepresentativeOrganizer organizer) {
    return new RepresentativeOrganizerVm(
        organizer.getId(),
        organizer.getOrganizationName(),
        organizer.getRepresentativeName(),
        organizer.getRepresentativeEmail(),
        organizer.getRepresentativePhone(),
        organizer.getRepresentativePosition());
  }
}
