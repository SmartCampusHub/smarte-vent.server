package com.winnguyen1905.activity.rest.service;

import org.springframework.stereotype.Service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.exception.BadRequestException;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EOrganization;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.OrganizationRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized authorization service to handle access control across the application.
 * Provides consistent authorization logic for different resources and operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ActivityRepository activityRepository;
    private final OrganizationRepository organizationRepository;
    private final ParticipationDetailRepository participationDetailRepository;

    private static final String ACCESS_DENIED = "Access denied to this resource";
    private static final String ADMIN_ONLY = "This operation is restricted to administrators";
    private static final String ORGANIZATION_ONLY = "This operation is restricted to organizations";

    /**
     * Validates if user has admin role
     */
    public void requireAdmin(TAccountRequest accountRequest) {
        if (accountRequest.getRole() != AccountRole.ADMIN) {
            log.warn("Access denied: User {} attempted admin operation without admin role", accountRequest.getId());
            throw new BadRequestException(ADMIN_ONLY);
        }
    }

    /**
     * Validates if user has organization role
     */
    public void requireOrganization(TAccountRequest accountRequest) {
        if (accountRequest.getRole() != AccountRole.ORGANIZATION) {
            log.warn("Access denied: User {} attempted organization operation without organization role", accountRequest.getId());
            throw new BadRequestException(ORGANIZATION_ONLY);
        }
    }

    /**
     * Validates if user can access activity (admin, activity creator, or organization owner)
     */
    public void validateActivityAccess(Long activityId, TAccountRequest accountRequest) {
        // Admin can access all activities
        if (accountRequest.getRole() == AccountRole.ADMIN) {
            return;
        }

        EActivity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        // Check if user is the activity creator
        if (activity.getCreatedById() != null && activity.getCreatedById().equals(accountRequest.getId())) {
            return;
        }

        // Check if user is the organization owner
        if (activity.getOrganization() != null && 
            activity.getOrganization().getId().equals(accountRequest.getId())) {
            return;
        }

        log.warn("Access denied: User {} attempted to access activity {} without permission", 
                 accountRequest.getId(), activityId);
        throw new BadRequestException(ACCESS_DENIED);
    }

    /**
     * Validates if user can modify activity (admin, activity creator, or organization owner)
     */
    public void validateActivityModificationAccess(Long activityId, TAccountRequest accountRequest) {
        // Admin can modify all activities
        if (accountRequest.getRole() == AccountRole.ADMIN) {
            return;
        }

        // Only organizations can create/modify activities
        if (accountRequest.getRole() != AccountRole.ORGANIZATION) {
            throw new BadRequestException("Only organizations can modify activities");
        }

        EActivity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        // Check if user is the organization owner of this activity
        if (activity.getOrganization() == null || 
            !activity.getOrganization().getId().equals(accountRequest.getId())) {
            log.warn("Access denied: Organization {} attempted to modify activity {} owned by different organization", 
                     accountRequest.getId(), activityId);
            throw new BadRequestException("You can only modify activities created by your organization");
        }
    }

    /**
     * Validates if user can access organization statistics
     */
    public void validateOrganizationAccess(Long organizationId, TAccountRequest accountRequest) {
        // Admin can access all organization data
        if (accountRequest.getRole() == AccountRole.ADMIN) {
            return;
        }

        // Organization can only access their own data
        if (accountRequest.getRole() == AccountRole.ORGANIZATION && 
            organizationId.equals(accountRequest.getId())) {
            return;
        }

        log.warn("Access denied: User {} attempted to access organization {} statistics without permission", 
                 accountRequest.getId(), organizationId);
        throw new BadRequestException(ACCESS_DENIED);
    }

    /**
     * Validates if user can access student statistics
     */
    public void validateStudentStatisticsAccess(Long studentId, TAccountRequest accountRequest) {
        // Admin can access all student statistics
        if (accountRequest.getRole() == AccountRole.ADMIN) {
            return;
        }

        // Student can only access their own statistics
        if (accountRequest.getRole() == AccountRole.STUDENT && 
            studentId.equals(accountRequest.getId())) {
            return;
        }

        log.warn("Access denied: User {} attempted to access student {} statistics without permission", 
                 accountRequest.getId(), studentId);
        throw new BadRequestException(ACCESS_DENIED);
    }

    /**
     * Validates if user can respond to feedback (admin or activity organization)
     */
    public void validateFeedbackResponseAccess(Long activityId, TAccountRequest accountRequest) {
        // Admin can respond to all feedback
        if (accountRequest.getRole() == AccountRole.ADMIN) {
            return;
        }

        // Only organizations can respond to feedback
        if (accountRequest.getRole() != AccountRole.ORGANIZATION) {
            throw new BadRequestException("Only organizations can respond to feedback");
        }

        EActivity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        // Check if user is the organization that owns the activity
        if (activity.getOrganization() == null || 
            !activity.getOrganization().getId().equals(accountRequest.getId())) {
            log.warn("Access denied: Organization {} attempted to respond to feedback for activity {} owned by different organization", 
                     accountRequest.getId(), activityId);
            throw new BadRequestException("You can only respond to feedback for your organization's activities");
        }
    }

    /**
     * Validates if user can verify participants (admin or activity contributor)
     */
    public void validateParticipationVerificationAccess(Long activityId, TAccountRequest accountRequest) {
        // Admin can verify all participations
        if (accountRequest.getRole() == AccountRole.ADMIN) {
            return;
        }

        // Check if user is a contributor to this specific activity
        boolean isContributor = participationDetailRepository.existsByParticipantIdAndActivityIdAndParticipationRole(
            accountRequest.getId(), activityId, ParticipationRole.CONTRIBUTOR);

        if (!isContributor) {
            log.warn("Access denied: User {} attempted to verify participation for activity {} without being a contributor", 
                     accountRequest.getId(), activityId);
            throw new BadRequestException("You must be a contributor to this activity to verify participants");
        }
    }

    /**
     * Validates if user can access their own resources or is admin
     */
    public void validateSelfOrAdmin(Long targetUserId, TAccountRequest accountRequest) {
        if (accountRequest.getRole() == AccountRole.ADMIN || 
            targetUserId.equals(accountRequest.getId())) {
            return;
        }

        log.warn("Access denied: User {} attempted to access resources of user {} without permission", 
                 accountRequest.getId(), targetUserId);
        throw new BadRequestException(ACCESS_DENIED);
    }

    /**
     * Validates if user owns the organization or is admin
     */
    public void validateOrganizationOwnership(Long organizationId, TAccountRequest accountRequest) {
        // Admin can access all organizations
        if (accountRequest.getRole() == AccountRole.ADMIN) {
            return;
        }

        // Only organizations can manage organization data
        if (accountRequest.getRole() != AccountRole.ORGANIZATION) {
            throw new BadRequestException("Only organizations can manage organization data");
        }

        // Organization can only manage their own data
        if (!organizationId.equals(accountRequest.getId())) {
            log.warn("Access denied: Organization {} attempted to manage organization {} without permission", 
                     accountRequest.getId(), organizationId);
            throw new BadRequestException("You can only manage your own organization");
        }
    }

    /**
     * Validates if user can access activity details based on their role
     */
    public void validateActivityViewAccess(Long activityId, TAccountRequest accountRequest) {
        // For viewing activities, we're more permissive:
        // - Admin can view all
        // - Organizations can view all (for potential collaboration)
        // - Students can view all (for participation)
        // This method is mainly for future extensibility if we need more restrictive viewing rules
        
        EActivity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));
        
        // Currently all authenticated users can view activities
        // But we validate the activity exists
    }
} 
