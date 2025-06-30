package com.winnguyen1905.activity.rest.service.impl;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.exception.BadRequestException;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.dto.ParticipationSearchParams;
import com.winnguyen1905.activity.model.dto.JoinActivityRequest;
import com.winnguyen1905.activity.model.dto.ParticipationUpdateDto;
import com.winnguyen1905.activity.model.viewmodel.ActivityVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.model.viewmodel.ParticipationDetailVm;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.persistance.repository.specification.EParticipationDetailSpecification;
import com.winnguyen1905.activity.rest.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing participant operations.
 * Handles participation in activities including verification, rejection, and history tracking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantServiceImpl implements ParticipantService {

  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final ParticipationDetailRepository participationDetailRepository;

  private static final String PARTICIPATION_NOT_FOUND = "Participation detail not found with ID: %d";
  private static final String ACCOUNT_NOT_FOUND = "Account not found with ID: %d";
  private static final String ACTIVITY_NOT_FOUND = "Activity not found with ID: %d";
  private static final String UNAUTHORIZED_ACCESS = "You don't have permission to perform this action";

  /**
   * Creates a new participant registration for an activity.
   * Note: Implementation needs to be completed based on business requirements.
   *
   * @param accountRequest The account request context
   * @param participantDto The participation parameters
   */
  @Override
  public void createParticipant(TAccountRequest accountRequest, ParticipationSearchParams participantDto) {
    log.info("Creating participant for account: {}", accountRequest.getId());
    
    // TODO: Implement participant creation logic
    // This should include:
    // 1. Validate account exists
    // 2. Validate activity exists and is available for registration
    // 3. Check if participant already registered
    // 4. Create participation record with UNVERIFIED status
    
    throw new UnsupportedOperationException("Participant creation not yet implemented");
  }

  /**
   * Updates participant information.
   * Note: Implementation needs to be completed based on business requirements.
   *
   * @param accountRequest The account request context
   * @param participantDto The updated participation parameters
   * @param participationId The ID of the participation to update
   */
  @Override
  public void updateParticipant(TAccountRequest accountRequest, ParticipationSearchParams participantDto, Long participationId) {
    log.info("Updating participant with ID: {} by account: {}", participationId, accountRequest.getId());
    
    // TODO: Implement participant update logic
    throw new UnsupportedOperationException("Participant update not yet implemented");
  }

  /**
   * Deletes a participant record.
   *
   * @param accountRequest The account request context
   * @param participationId The ID of the participation to delete
   * @throws ResourceNotFoundException if the participation is not found
   */
  @Override
  public void deleteParticipant(TAccountRequest accountRequest, Long participationId) {
    log.info("Deleting participant with ID: {} by account: {}", participationId, accountRequest.getId());
    
    validateParticipationExists(participationId);
    
    participationDetailRepository.deleteById(participationId);
    
    log.info("Participant deleted successfully with ID: {}", participationId);
  }

  /**
   * Retrieves a participant by ID.
   *
   * @param participationId The ID of the participation to retrieve
   * @return The participation detail view model
   * @throws ResourceNotFoundException if the participation is not found
   */
  @Override
  @Transactional(readOnly = true)
  public ParticipationDetailVm getParticipantById(Long participationId) {
    log.debug("Retrieving participant with ID: {}", participationId);
    
    EParticipationDetail participationDetail = findParticipationById(participationId);
    
    return mapToParticipationDetailVm(participationDetail);
  }

  /**
   * Retrieves participants by activity ID.
   * Note: Implementation needs to be completed.
   *
   * @param activityId The ID of the activity
   * @return List of participants for the activity
   */
  @Override
  @Transactional(readOnly = true)
  public List<ParticipationDetailVm> getParticipantsByActivityId(Long activityId) {
    log.debug("Retrieving participants for activity ID: {}", activityId);
    
    // TODO: Implement retrieval of participants by activity ID
    throw new UnsupportedOperationException("Get participants by activity ID not yet implemented");
  }

  /**
   * Verifies a participation request.
   *
   * @param accountRequest The account request context
   * @param updateDto The verification update data
   * @return The updated participation detail view model
   * @throws ResourceNotFoundException if the participation is not found
   * @throws BadRequestException if the user doesn't have permission
   */
  @Override
  public ParticipationDetailVm verifyParticipation(TAccountRequest accountRequest, ParticipationUpdateDto updateDto) {
    log.info("Verifying participation ID: {} by account: {}", updateDto.getParticipationId(), accountRequest.getId());
    
    validateUpdateData(updateDto);
    
    EParticipationDetail participationDetail = findParticipationById(updateDto.getParticipationId());
    
    validateUserPermission(accountRequest.getId(), participationDetail);
    
    updateParticipationStatus(participationDetail, updateDto, accountRequest.getUsername());
    
    EParticipationDetail savedDetail = participationDetailRepository.save(participationDetail);
    
    log.info("Participation verified successfully with ID: {}", savedDetail.getId());
    
    return mapToParticipationDetailVm(savedDetail);
  }

  /**
   * Rejects a participation request.
   *
   * @param accountRequest The account request context
   * @param updateDto The rejection update data
   * @return The updated participation detail view model
   * @throws ResourceNotFoundException if the participation is not found
   * @throws BadRequestException if the user doesn't have permission
   */
  @Override
  public ParticipationDetailVm rejectParticipation(TAccountRequest accountRequest, ParticipationUpdateDto updateDto) {
    log.info("Rejecting participation ID: {} by account: {}", updateDto.getParticipationId(), accountRequest.getId());
    
    validateUpdateData(updateDto);
    
    EParticipationDetail participationDetail = findParticipationById(updateDto.getParticipationId());
    
    validateUserPermission(accountRequest.getId(), participationDetail);
    
    updateParticipationStatus(participationDetail, updateDto, accountRequest.getUsername());
    
    EParticipationDetail savedDetail = participationDetailRepository.save(participationDetail);
    
    log.info("Participation rejected successfully with ID: {}", savedDetail.getId());
    
    return mapToParticipationDetailVm(savedDetail);
  }

  /**
   * Retrieves paginated participation detail histories based on search criteria.
   *
   * @param accountRequest The account request context
   * @param participationSearchParams The search parameters
   * @param pageable The pagination information
   * @return Paginated response of participation details
   */
  @Override
  @Transactional(readOnly = true)
  public PagedResponse<ParticipationDetailVm> getParticipantDetailHistories(
      TAccountRequest accountRequest,
      ParticipationSearchParams participationSearchParams,
      Pageable pageable) {
    
    log.debug("Retrieving participation histories for account: {}", accountRequest.getId());
    
    Specification<EParticipationDetail> spec = EParticipationDetailSpecification.filterBy(participationSearchParams);
    Page<EParticipationDetail> participationDetails = participationDetailRepository.findAll(spec, pageable);

    List<ParticipationDetailVm> participationDetailVms = participationDetails.getContent().stream()
        .map(this::mapToParticipationDetailVm)
        .collect(Collectors.toList());

    return buildPagedResponse(participationDetails, participationDetailVms);
  }

  /**
   * Validates that participation exists.
   *
   * @param participationId The participation ID to validate
   * @throws ResourceNotFoundException if the participation doesn't exist
   */
  private void validateParticipationExists(Long participationId) {
    if (!participationDetailRepository.existsById(participationId)) {
      throw new ResourceNotFoundException(String.format(PARTICIPATION_NOT_FOUND, participationId));
    }
  }

  /**
   * Validates participation update data.
   *
   * @param updateDto The update data to validate
   * @throws BadRequestException if the data is invalid
   */
  private void validateUpdateData(ParticipationUpdateDto updateDto) {
    if (updateDto == null) {
      throw new BadRequestException("Update data cannot be null");
    }
    
    if (updateDto.getParticipationId() == null) {
      throw new BadRequestException("Participation ID is required");
    }
    
    if (updateDto.getStatus() == null) {
      throw new BadRequestException("Status is required");
    }
  }

  /**
   * Finds a participation detail by ID.
   *
   * @param participationId The participation ID to find
   * @return The found participation detail
   * @throws ResourceNotFoundException if not found
   */
  private EParticipationDetail findParticipationById(Long participationId) {
    return participationDetailRepository.findById(participationId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(PARTICIPATION_NOT_FOUND, participationId)));
  }

  /**
   * Validates user permission to modify the participation.
   *
   * @param userId The user ID performing the action
   * @param participationDetail The participation detail to check
   * @throws BadRequestException if the user doesn't have permission
   */
  private void validateUserPermission(Long userId, EParticipationDetail participationDetail) {
    boolean isContributor = participationDetailRepository.existsByParticipantIdAndActivityIdAndParticipationRole(
        userId, participationDetail.getActivity().getId(), ParticipationRole.CONTRIBUTOR);
    
    if (!isContributor) {
      throw new BadRequestException(UNAUTHORIZED_ACCESS);
    }
  }

  /**
   * Updates participation status and related fields.
   *
   * @param participationDetail The participation detail to update
   * @param updateDto The update data
   * @param processorUsername The username of the processor
   */
  private void updateParticipationStatus(EParticipationDetail participationDetail, 
                                       ParticipationUpdateDto updateDto, 
                                       String processorUsername) {
    participationDetail.setParticipationStatus(updateDto.getStatus());
    participationDetail.setProcessedAt(Instant.now());
    participationDetail.setProcessedBy(processorUsername);
    
    if (updateDto.getVerifiedNote() != null) {
      participationDetail.setVerifiedNote(updateDto.getVerifiedNote());
    }
    
    if (updateDto.getRejectionReason() != null) {
      participationDetail.setRejectionReason(updateDto.getRejectionReason());
    }
  }

  /**
   * Maps participation detail entity to view model.
   *
   * @param participationDetail The entity to map
   * @return The mapped view model
   */
  private ParticipationDetailVm mapToParticipationDetailVm(EParticipationDetail participationDetail) {
    return ParticipationDetailVm.builder()
        .id(participationDetail.getId())
        .activityId(participationDetail.getActivity().getId())
        .activityName(participationDetail.getActivity().getActivityName())
        .activityCategory(participationDetail.getActivity().getActivityCategory())
        .activityStatus(participationDetail.getActivity().getStatus())
        .activityVenue(participationDetail.getActivity().getVenue())
        .startDate(participationDetail.getActivity().getStartDate())
        .endDate(participationDetail.getActivity().getEndDate())
        .registrationTime(participationDetail.getRegisteredAt())
        .participationRole(participationDetail.getParticipationRole())
        .participationStatus(participationDetail.getParticipationStatus())
        .processedAt(participationDetail.getProcessedAt())
        .processedBy(participationDetail.getProcessedBy())
        .rejectionReason(participationDetail.getRejectionReason())
        .verifiedNote(participationDetail.getVerifiedNote())
        .studentId(participationDetail.getParticipant().getId())
        .participantName(participationDetail.getParticipant().getFullName())
        .identifyCode(participationDetail.getParticipant().getIdentifyCode())
        .build();
  }

  /**
   * Builds a paged response from page data and view models.
   *
   * @param page The page data
   * @param content The view models
   * @return The paged response
   */
  private PagedResponse<ParticipationDetailVm> buildPagedResponse(
      Page<EParticipationDetail> page, 
      List<ParticipationDetailVm> content) {
    
    return PagedResponse.<ParticipationDetailVm>builder()
        .maxPageItems(page.getSize())
        .page(page.getNumber())
        .size(page.getSize())
        .results(content)
        .totalElements((int) page.getTotalElements())
        .totalPages(page.getTotalPages())
        .build();
  }
}
