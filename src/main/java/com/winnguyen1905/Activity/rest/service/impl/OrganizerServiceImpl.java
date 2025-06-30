package com.winnguyen1905.activity.rest.service.impl;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.exception.BadRequestException;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.dto.OrganizationDto;
import com.winnguyen1905.activity.model.dto.OrganizationSearchRequest;
import com.winnguyen1905.activity.model.viewmodel.OrganizationVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.persistance.entity.EOrganization;
import com.winnguyen1905.activity.persistance.repository.RepresentativeOrganizerRepository;
import com.winnguyen1905.activity.persistance.repository.specification.OrganizationSpecification;
import com.winnguyen1905.activity.rest.service.OrganizerService;
import com.winnguyen1905.activity.rest.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Service implementation for managing organizers.
 * Provides functionality for creating, updating, deleting, and retrieving organization data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrganizerServiceImpl implements OrganizerService {

  private final RepresentativeOrganizerRepository organizerRepository;
  private final AuthorizationService authorizationService;

  private static final String ORGANIZER_NOT_FOUND = "Organizer not found with ID: %d";

  /**
   * Creates a new organizer organization.
   *
   * @param accountRequest The account request context
   * @param organizerDto The organizer data to create
   * @throws BadRequestException if the organizer data is invalid
   */
  @Override
  public void createOrganizer(TAccountRequest accountRequest, OrganizationDto organizerDto) {
    log.info("Creating organizer for account: {}", accountRequest.getId());
    
    validateOrganizerData(organizerDto);
    
    EOrganization organizer = buildOrganizer(organizerDto);
    
    organizerRepository.save(organizer);
    
    log.info("Organizer created successfully with ID: {}", organizer.getId());
  }

  /**
   * Updates an existing organizer organization.
   *
   * @param accountRequest The account request context
   * @param organizerDto The updated organizer data
   * @throws BadRequestException if the organizer data is invalid
   * @throws ResourceNotFoundException if the organizer is not found
   */
  @Override
  public void updateOrganizer(TAccountRequest accountRequest, OrganizationDto organizerDto) {
    log.info("Updating organizer with ID: {} for account: {}", organizerDto.getId(), accountRequest.getId());
    
    // Authorization check: Only admins or the organization itself can update organization data
    authorizationService.validateOrganizationOwnership(organizerDto.getId(), accountRequest);
    
    validateOrganizerDataForUpdate(organizerDto);
    
    EOrganization organizer = findOrganizerById(organizerDto.getId());
    
    updateOrganizerFields(organizer, organizerDto);
    
    organizerRepository.save(organizer);
    
    log.info("Organizer updated successfully with ID: {}", organizer.getId());
  }

  /**
   * Deletes an organizer by ID.
   *
   * @param accountRequest The account request context
   * @param organizerId The ID of the organizer to delete
   * @throws ResourceNotFoundException if the organizer is not found
   */
  @Override
  public void deleteOrganizer(TAccountRequest accountRequest, Long organizerId) {
    log.info("Deleting organizer with ID: {} for account: {}", organizerId, accountRequest.getId());
    
    // Authorization check: Only admins or the organization itself can delete organization data
    authorizationService.validateOrganizationOwnership(organizerId, accountRequest);
    
    validateOrganizerExists(organizerId);
    
    organizerRepository.deleteById(organizerId);
    
    log.info("Organizer deleted successfully with ID: {}", organizerId);
  }

  /**
   * Retrieves organizers with search criteria and pagination.
   *
   * @param organizationSearchRequest The search criteria
   * @param pageable The pagination information
   * @return Paginated response of organizers
   */
  @Override
  @Transactional(readOnly = true)
  public PagedResponse<OrganizationVm> getAllOrganizers(OrganizationSearchRequest organizationSearchRequest,
      Pageable pageable) {
    
    log.debug("Retrieving organizers with search criteria: {}", organizationSearchRequest);
    
    Specification<EOrganization> spec = OrganizationSpecification.search(organizationSearchRequest);
    Page<EOrganization> page = organizerRepository.findAll(spec, pageable);

    List<OrganizationVm> content = page.getContent().stream()
        .map(this::mapToViewModel)
        .collect(Collectors.toList());

    return buildPagedResponse(page, content);
  }

  /**
   * Retrieves an organizer by ID.
   *
   * @param organizerId The ID of the organizer to retrieve
   * @return The organizer view model
   * @throws ResourceNotFoundException if the organizer is not found
   */
  @Override
  @Transactional(readOnly = true)
  public OrganizationVm getOrganizerById(Long organizerId) {
    log.debug("Retrieving organizer with ID: {}", organizerId);
    
    EOrganization organizer = findOrganizerById(organizerId);
    
    return mapToViewModel(organizer);
  }

  /**
   * Deletes an organizer by ID (alternative method).
   *
   * @param organizerId The ID of the organizer to delete
   * @throws ResourceNotFoundException if the organizer is not found
   */
  @Override
  public void deleteOrganizerById(Long organizerId) {
    log.info("Deleting organizer with ID: {}", organizerId);
    
    validateOrganizerExists(organizerId);
    
    organizerRepository.deleteById(organizerId);
    
    log.info("Organizer deleted successfully with ID: {}", organizerId);
  }

  /**
   * Retrieves organizers with page-based pagination.
   *
   * @param page The page number (0-based)
   * @param size The page size
   * @return Paginated response of organizers
   */
  @Override
  @Transactional(readOnly = true)
  public PagedResponse<OrganizationVm> getAllOrganizersByPage(int page, int size) {
    log.debug("Retrieving organizers by page: {} with size: {}", page, size);
    
    Pageable pageable = PageRequest.of(page, size);
    Page<EOrganization> organizersPage = organizerRepository.findAll(pageable);

    List<OrganizationVm> content = organizersPage.getContent().stream()
        .map(this::mapToViewModel)
        .collect(Collectors.toList());

    return buildPagedResponse(organizersPage, content);
  }

  /**
   * Validates organizer data for creation.
   *
   * @param organizerDto The organizer data to validate
   * @throws BadRequestException if the data is invalid
   */
  private void validateOrganizerData(OrganizationDto organizerDto) {
    if (organizerDto == null) {
      throw new BadRequestException("Organizer data cannot be null");
    }
    
    if (!StringUtils.hasText(organizerDto.getOrganizationName())) {
      throw new BadRequestException("Organization name is required");
    }
    
    if (!StringUtils.hasText(organizerDto.getRepresentativeEmail())) {
      throw new BadRequestException("Representative email is required");
    }
    
    if (!StringUtils.hasText(organizerDto.getRepresentativePhone())) {
      throw new BadRequestException("Representative phone is required");
    }
    
    if (organizerDto.getOrganizationType() == null) {
      throw new BadRequestException("Organization type is required");
    }
  }

  /**
   * Validates organizer data for update.
   *
   * @param organizerDto The organizer data to validate
   * @throws BadRequestException if the data is invalid
   */
  private void validateOrganizerDataForUpdate(OrganizationDto organizerDto) {
    validateOrganizerData(organizerDto);
    
    if (organizerDto.getId() == null) {
      throw new BadRequestException("Organizer ID is required for update");
    }
  }



  /**
   * Validates that an organizer exists.
   *
   * @param organizerId The organizer ID to validate
   * @throws ResourceNotFoundException if the organizer doesn't exist
   */
  private void validateOrganizerExists(Long organizerId) {
    if (!organizerRepository.existsById(organizerId)) {
      throw new ResourceNotFoundException(String.format(ORGANIZER_NOT_FOUND, organizerId));
    }
  }

  /**
   * Finds an organizer by ID.
   *
   * @param organizerId The organizer ID to find
   * @return The found organizer
   * @throws ResourceNotFoundException if the organizer is not found
   */
  private EOrganization findOrganizerById(Long organizerId) {
    return organizerRepository.findById(organizerId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(ORGANIZER_NOT_FOUND, organizerId)));
  }

  /**
   * Builds an organizer entity from DTO.
   *
   * @param organizerDto The organizer data
   * @return The built organizer entity
   */
  private EOrganization buildOrganizer(OrganizationDto organizerDto) {
    return EOrganization.builder()
        .name(organizerDto.getOrganizationName())
        .email(organizerDto.getRepresentativeEmail())
        .phone(organizerDto.getRepresentativePhone())
        .type(organizerDto.getOrganizationType())
        .build();
  }

  /**
   * Updates organizer fields from DTO.
   *
   * @param organizer The organizer entity to update
   * @param organizerDto The new organizer data
   */
  private void updateOrganizerFields(EOrganization organizer, OrganizationDto organizerDto) {
    organizer.setName(organizerDto.getOrganizationName());
    organizer.setEmail(organizerDto.getRepresentativeEmail());
    organizer.setPhone(organizerDto.getRepresentativePhone());
    organizer.setType(organizerDto.getOrganizationType());
  }

  /**
   * Maps an organizer entity to view model.
   *
   * @param organizer The organizer entity to map
   * @return The mapped view model
   */
  private OrganizationVm mapToViewModel(EOrganization organizer) {
    return OrganizationVm.builder()
        .id(organizer.getId())
        .organizationName(organizer.getName())
        .representativeEmail(organizer.getEmail())
        .representativePhone(organizer.getPhone())
        .organizationType(organizer.getType())
        .build();
  }

  /**
   * Builds a paged response from page data and content.
   *
   * @param page The page data
   * @param content The content list
   * @return The paged response
   */
  private PagedResponse<OrganizationVm> buildPagedResponse(Page<EOrganization> page, List<OrganizationVm> content) {
    return PagedResponse.<OrganizationVm>builder()
        .results(content)
        .totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements())
        .size(page.getSize())
        .page(page.getNumber())
        .build();
  }
}
