package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.exception.BusinessLogicException;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.dto.FeedbackCreateDto;
import com.winnguyen1905.activity.model.dto.FeedbackUpdateDto;
import com.winnguyen1905.activity.model.viewmodel.FeedbackDetailVm;
import com.winnguyen1905.activity.model.viewmodel.FeedbackSummaryVm;
import com.winnguyen1905.activity.model.dto.OrganizationResponseDto;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EFeedback;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.rest.service.FeedbackService;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ParticipationDetailRepository participationDetailRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public FeedbackDetailVm createFeedback(TAccountRequest accountRequest, FeedbackCreateDto feedbackDto) {
        // Validate if activity exists
        EActivity activity = activityRepository.findById(feedbackDto.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activity not found with id: " + feedbackDto.getActivityId()));

        // Validate if participation exists
        Optional<EParticipationDetail> participation = participationDetailRepository
                .findByStudentIdAndActivityId(accountRequest.getId(), feedbackDto.getActivityId());

        if (participation.isEmpty()) {
            throw new ResourceNotFoundException("Participation not found with id: " + feedbackDto.getParticipationId());
        }

        // Check if the participation belongs to the student
        if (!participation.get().getParticipant().getId().equals(accountRequest.getId())) {
            throw new BusinessLogicException("You can only provide feedback for your own participation");
        }

        // Check if the participation is for the specified activity
        if (!participation.get().getActivity().getId().equals(activity.getId())) {
            throw new BusinessLogicException("The participation is not for the specified activity");
        }

        // Check if the participation status is UNVERIFIED (previously verified to be
        // attended)
        // if (participation.getParticipationStatus() != ParticipationStatus.UNVERIFIED)
        // {
        // throw new BusinessLogicException("You can only provide feedback for
        // activities you have attended");
        // }

        // Check if feedback already exists for this participation
        if (!participation.get().getFeedbacks().isEmpty()) {
            throw new BusinessLogicException("Feedback already exists for this participation");
        }

        // Create new feedback
        EFeedback feedback = new EFeedback();
        feedback.setActivity(activity);
        feedback.setParticipation(participation.get());
        feedback.setRating(feedbackDto.getRating());
        feedback.setFeedbackDescription(feedbackDto.getFeedbackDescription());

        // Save feedback
        EFeedback savedFeedback = feedbackRepository.save(feedback);

        // Return the view model
        return mapToDetailVm(savedFeedback);
    }

    @Override
    public FeedbackDetailVm getFeedbackById(Long feedbackId) {
        EFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackId));

        return mapToDetailVm(feedback);
    }

    @Override
    @Transactional
    public FeedbackDetailVm updateFeedback(TAccountRequest accountRequest, FeedbackUpdateDto feedbackDto) {
        EFeedback feedback = feedbackRepository.findById(feedbackDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackDto.getId()));

        // // Update only non-null fields
        // if (feedbackDto.getRating() != null) {
        //     feedback.setRating(feedbackDto.getRating());
        // }

        // if (feedbackDto.getFeedbackDescription() != null) {
        //     feedback.setFeedbackDescription(feedbackDto.getFeedbackDescription());
        // }

        feedback.setOrganizationResponse(feedbackDto.getOrganizationResponse());

        // Save updated feedback
        EFeedback updatedFeedback = feedbackRepository.save(feedback);

        return mapToDetailVm(updatedFeedback);
    }

    @Override
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new ResourceNotFoundException("Feedback not found with id: " + feedbackId);
        }

        feedbackRepository.deleteById(feedbackId);
    }

    @Override
    public Page<FeedbackSummaryVm> getStudentFeedbacks(Long studentId, Pageable pageable) {
        // Use efficient database query instead of in-memory filtering
        Page<EFeedback> feedbacks = feedbackRepository.findByStudentId(studentId, pageable);

        // Map to view models
        List<FeedbackSummaryVm> feedbackVms = feedbacks.getContent().stream()
                .map(this::mapToSummaryVm)
                .collect(Collectors.toList());

        return new PageImpl<>(feedbackVms, pageable, feedbacks.getTotalElements());
    }

    @Override
    public boolean canStudentProvideFeedback(Long studentId, Long activityId) {
        // Use efficient database query instead of loading all participations
        Optional<EParticipationDetail> participation = participationDetailRepository
                .findByStudentIdAndActivityIdForFeedback(studentId, activityId);

        // Student can provide feedback if they have participated (VERIFIED status)
        // and haven't provided feedback yet
        return participation.isPresent() &&
                participation.get().getParticipationStatus() == ParticipationStatus.VERIFIED &&
                participation.get().getFeedbacks().isEmpty();
    }

    @Override
    public Page<FeedbackSummaryVm> getActivityFeedbacks(Long activityId, Pageable pageable) {
        // Check if activity exists
        if (!activityRepository.existsById(activityId)) {
            throw new ResourceNotFoundException("Activity not found with id: " + activityId);
        }

        // Use efficient database query instead of in-memory filtering
        Page<EFeedback> feedbacks = feedbackRepository.findByActivityId(activityId, pageable);

        // Map to view models
        List<FeedbackSummaryVm> feedbackVms = feedbacks.getContent().stream()
                .map(this::mapToSummaryVm)
                .collect(Collectors.toList());

        return new PageImpl<>(feedbackVms, pageable, feedbacks.getTotalElements());
    }

    @Override
    public Double getAverageRatingForActivity(Long activityId) {
        return feedbackRepository.getAverageRatingForActivity(activityId);
    }

    @Override
    public Page<FeedbackSummaryVm> getOrganizationFeedbacks(Long organizationId, Pageable pageable) {
        // Use efficient database query instead of in-memory filtering
        Page<EFeedback> feedbacks = feedbackRepository.findByOrganizationId(organizationId, pageable);

        // Map to view models
        List<FeedbackSummaryVm> feedbackVms = feedbacks.getContent().stream()
                .map(this::mapToSummaryVm)
                .collect(Collectors.toList());

        return new PageImpl<>(feedbackVms, pageable, feedbacks.getTotalElements());
    }

    @Override
    public Double getAverageRatingForOrganization(Long organizationId) {
        return feedbackRepository.getAverageRatingForOrganization(organizationId);
    }

    @Override
    public List<Object[]> getAverageRatingByCategoryForOrganization(Long organizationId) {
        return feedbackRepository.getAverageRatingByCategoryForOrganization(organizationId);
    }

    @Override
    public List<Object[]> getRatingTrendByMonthForOrganization(Long organizationId, Instant startDate,
            Instant endDate) {
        return feedbackRepository.getRatingTrendByMonthForOrganization(organizationId, startDate, endDate);
    }

    @Override
    public Page<FeedbackSummaryVm> getFilteredFeedbacks(
            Instant startDate,
            Instant endDate,
            ActivityCategory category,
            ActivityStatus status,
            Long organizationId,
            Pageable pageable) {

        // Use efficient database query instead of in-memory filtering
        Page<EFeedback> feedbacks = feedbackRepository.findFilteredFeedbacks(
                startDate, endDate, category, status, organizationId, pageable);

        // Map to view models
        List<FeedbackSummaryVm> feedbackVms = feedbacks.getContent().stream()
                .map(this::mapToSummaryVm)
                .collect(Collectors.toList());

        return new PageImpl<>(feedbackVms, pageable, feedbacks.getTotalElements());
    }

    @Override
    public List<String> getKeywordAnalysis(Long organizationId) {
        // Get all feedback descriptions for the organization
        List<String> feedbackDescriptions = feedbackRepository.getFeedbackDescriptionsForOrganization(organizationId);

        // Simple keyword extraction - split by space and count occurrences
        Map<String, Integer> wordFrequency = new HashMap<>();

        for (String description : feedbackDescriptions) {
            if (description == null || description.trim().isEmpty()) {
                continue;
            }

            String[] words = description.toLowerCase().split("\\s+");
            for (String word : words) {
                // Clean word and ignore short words
                word = word.replaceAll("[^a-zA-Z]", "");
                if (word.length() > 3) {
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                }
            }
        }

        // Return top keywords
        return wordFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<Object[]> getBestRatedActivitiesForOrganization(Long organizationId, Long minFeedbacks,
            Pageable pageable) {
        return feedbackRepository.getBestRatedActivitiesForOrganization(organizationId, minFeedbacks, pageable);
    }

    @Override
    @Transactional
    public FeedbackDetailVm addOrganizationResponse(Long feedbackId, OrganizationResponseDto responseDto) {
        EFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackId));

        // TODO: Check if feedback already has a response
        // if (feedback.getOrganizationResponse() != null) {
        //     throw new BusinessLogicException("This feedback already has an organization response");
        // }

        // Add organization response
        feedback.setOrganizationResponse(responseDto.getResponse());
        feedback.setRespondedAt(Instant.now());

        // Save updated feedback
        EFeedback updatedFeedback = feedbackRepository.save(feedback);

        return mapToDetailVm(updatedFeedback);
    }

    // Helper methods to map entities to view models
    private FeedbackDetailVm mapToDetailVm(EFeedback feedback) {
        EActivity activity = feedback.getActivity();
        EParticipationDetail participation = feedback.getParticipation();
        EAccountCredentials student = participation.getParticipant();

        return FeedbackDetailVm.builder()
                .id(feedback.getId())
                .activityId(activity.getId())
                .activityName(activity.getActivityName())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .rating(feedback.getRating())
                .feedbackDescription(feedback.getFeedbackDescription())
                .participationId(participation.getId())
                .organizationResponse(feedback.getOrganizationResponse())
                .respondedAt(feedback.getRespondedAt())
                .hasResponse(feedback.getOrganizationResponse() != null)
                .build();
    }

    private FeedbackSummaryVm mapToSummaryVm(EFeedback feedback) {
        EActivity activity = feedback.getActivity();
        EParticipationDetail participation = feedback.getParticipation();
        EAccountCredentials student = participation.getParticipant();

        return FeedbackSummaryVm.builder()
                .id(feedback.getId())
                .activityId(activity.getId())
                .activityName(activity.getActivityName())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .rating(feedback.getRating())
                .createdDate(feedback.getCreatedAt())
                .organizationResponse(feedback.getOrganizationResponse())
                .respondedAt(feedback.getRespondedAt())
                .hasResponse(feedback.getOrganizationResponse() != null)
                .build();
    }
}
