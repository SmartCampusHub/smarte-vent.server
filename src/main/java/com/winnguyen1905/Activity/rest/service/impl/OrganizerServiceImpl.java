package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.OrganizationDto;
import com.winnguyen1905.Activity.model.dto.OrganizationSearchRequest;
import com.winnguyen1905.Activity.model.viewmodel.OrganizationVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.model.viewmodel.RepresentativeOrganizerVm;
import com.winnguyen1905.Activity.persistance.entity.EOrganization;
import com.winnguyen1905.Activity.persistance.repository.RepresentativeOrganizerRepository;
import com.winnguyen1905.Activity.persistance.repository.specification.OrganizationSpecification;
import com.winnguyen1905.Activity.rest.service.OrganizerService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizerServiceImpl implements OrganizerService {

  private final RepresentativeOrganizerRepository organizerRepository;

  @Override
  public void createOrganizer(TAccountRequest accountRequest, OrganizationDto organizerDto) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'createOrganizer'");
  }

  @Override
  public void updateOrganizer(TAccountRequest accountRequest, OrganizationDto organizerDto, Long id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'updateOrganizer'");
  }

  @Override
  public void deleteOrganizer(TAccountRequest accountRequest, Long id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteOrganizer'");
  }

  @Override
  public PagedResponse<OrganizationVm> getAllOrganizers(OrganizationSearchRequest organizationSearchRequest,
      Pageable pageable) {

    Specification<EOrganization> spec = OrganizationSpecification.search(organizationSearchRequest);
    Page<EOrganization> page = organizerRepository.findAll(spec, pageable);

    List<OrganizationVm> content = page.getContent().stream()
        .map(this::mapToViewModel)
        .collect(Collectors.toList());

    return PagedResponse.<OrganizationVm>builder()
        .results(content)
        .totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements())
        .size(page.getSize())
        .page(page.getNumber())
        .build();
  }

  @Override
  public OrganizationVm getOrganizerById(Long id) {
    EOrganization organizer = organizerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Organizer not found with id: " + id));

    return mapToViewModel(organizer);
  }

  @Override
  public void deleteOrganizerById(Long id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'deleteOrganizerById'");
  }

  @Override
  public PagedResponse<OrganizationVm> getAllOrganizersByPage(int page, int size) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAllOrganizersByPage'");
  }

  // @Override
  // public void createOrganizer(TAccountRequest accountRequest, OrganizationDto
  // organizerDto) {
  // EOrganization organizer = EOrganization.builder()
  // .name(organizerDto.name())
  // .email(organizerDto.email())
  // .phone(organizerDto.phone())
  // .build();

  // organizerRepository.save(organizer);
  // }

  // @Override
  // public void updateOrganizer(TAccountRequest accountRequest, OrganizationDto
  // organizerDto, Long id) {
  // EOrganization organizer = organizerRepository.findById(id)
  // .orElseThrow(() -> new RuntimeException("Organizer not found with id: " +
  // id));

  // organizer.setOrganizationName(organizerDto.organizationName());
  // organizer.setRepresentativeEmail(organizerDto.representativeEmail());
  // organizer.setRepresentativePhone(organizerDto.representativePhone());

  // organizerRepository.save(organizer);
  // }

  // @Override
  // public void deleteOrganizer(TAccountRequest accountRequest, Long id) {
  // if (!organizerRepository.existsById(id)) {
  // throw new RuntimeException("Organizer not found with id: " + id);
  // }
  // organizerRepository.deleteById(id);
  // }

  // @Override
  // public RepresentativeOrganizerVm getOrganizerById(Long id) {
  // EOrganization organizer = organizerRepository.findById(id)
  // .orElseThrow(() -> new RuntimeException("Organizer not found with id: " +
  // id));

  // return mapToViewModel(organizer);
  // }

  // @Override
  // public List<RepresentativeOrganizerVm> getAllOrganizers() {
  // return organizerRepository.findAll().stream()
  // .map(this::mapToViewModel)
  // .toList();
  // }

  private OrganizationVm mapToViewModel(EOrganization organizer) {
    return OrganizationVm.builder()
        .id(organizer.getId())
        .organizationName(organizer.getName())
        .representativeEmail(organizer.getEmail())
        .representativePhone(organizer.getPhone())
        .organizationType(organizer.getType())
        .build();
  }
}
