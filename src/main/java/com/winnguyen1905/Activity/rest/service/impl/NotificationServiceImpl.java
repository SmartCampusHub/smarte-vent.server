package com.winnguyen1905.activity.rest.service.impl;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.exception.BadRequestException;
import com.winnguyen1905.activity.model.dto.NotificationDto;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.ENotification;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.NotificationRepository;
import com.winnguyen1905.activity.model.viewmodel.NotificationVm;
import com.winnguyen1905.activity.rest.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing notifications.
 * Provides functionality to send, retrieve, mark as read, and delete notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

  private final AccountRepository accountRepository;
  private final NotificationRepository notificationRepository;

  private static final String NOTIFICATION_NOT_FOUND = "Notification not found with ID: %d";
  private static final String ACCOUNT_NOT_FOUND = "Account not found with ID: %d";
  private static final String RECEIVER_NOT_FOUND = "Receiver not found with ID: %d";

  /**
   * Sends a notification to the specified receiver.
   *
   * @param accountRequest The account request context
   * @param notificationDto The notification data to send
   * @throws BadRequestException if the notification data is invalid
   * @throws ResourceNotFoundException if the receiver is not found
   */
  @Override
  public void sendNotification(NotificationDto notificationDto) {
    log.info("Sending notification to receiver ID: {}", notificationDto.getReceiverId());
    
    validateNotificationData(notificationDto);
    
    EAccountCredentials receiver = findAccountById(notificationDto.getReceiverId());
    
    ENotification notification = buildNotification(notificationDto, receiver);
    
    notificationRepository.save(notification);
    
    log.info("Notification sent successfully to receiver ID: {}", notificationDto.getReceiverId());
  }

  /**
   * Retrieves paginated notifications for the authenticated user.
   *
   * @param accountRequest The account request context
   * @param pageable Pagination information
   * @return Paginated response containing notifications
   * @throws ResourceNotFoundException if the account is not found
   */
  @Override
  @Transactional(readOnly = true)
  public PagedResponse<NotificationVm> getNotifications(TAccountRequest accountRequest, Pageable pageable) {
    log.debug("Retrieving notifications for account ID: {}", accountRequest.getId());
    
    EAccountCredentials account = findAccountById(accountRequest.getId());
    
    Page<ENotification> notificationsPage = notificationRepository
        .findByReceiverOrderByIsReadAscCreatedAtDesc(account, pageable);

    List<NotificationVm> notificationVms = mapNotificationsToViewModels(notificationsPage.getContent());

    return buildPagedResponse(notificationsPage, notificationVms);
  }

  /**
   * Marks a notification as read.
   *
   * @param accountRequest The account request context
   * @param notificationId The ID of the notification to mark as read
   * @throws ResourceNotFoundException if the notification is not found
   */
  @Override
  public void readNotification(TAccountRequest accountRequest, Long notificationId) {
    log.info("Marking notification as read - ID: {}, User: {}", notificationId, accountRequest.getId());
    
    ENotification notification = findNotificationById(notificationId);
    validateNotificationOwnership(notification, accountRequest.getId());
    
    if (!notification.getIsRead()) {
      notification.setIsRead(true);
      notificationRepository.save(notification);
      log.info("Notification marked as read successfully - ID: {}", notificationId);
    } else {
      log.debug("Notification already marked as read - ID: {}", notificationId);
    }
  }

  /**
   * Deletes a notification.
   *
   * @param accountRequest The account request context
   * @param notificationId The ID of the notification to delete
   * @throws ResourceNotFoundException if the notification is not found
   */
  @Override
  public void deleteNotification(TAccountRequest accountRequest, Long notificationId) {
    log.info("Deleting notification - ID: {}, User: {}", notificationId, accountRequest.getId());
    
    ENotification notification = findNotificationById(notificationId);
    validateNotificationOwnership(notification, accountRequest.getId());
    
    notificationRepository.delete(notification);
    
    log.info("Notification deleted successfully - ID: {}", notificationId);
  }

  /**
   * Validates notification data before processing.
   *
   * @param notificationDto The notification data to validate
   * @throws BadRequestException if the data is invalid
   */
  private void validateNotificationData(NotificationDto notificationDto) {
    if (notificationDto == null) {
      throw new BadRequestException("Notification data cannot be null");
    }
    
    if (!StringUtils.hasText(notificationDto.getTitle())) {
      throw new BadRequestException("Notification title cannot be empty");
    }
    
    if (!StringUtils.hasText(notificationDto.getContent())) {
      throw new BadRequestException("Notification content cannot be empty");
    }
    
    if (notificationDto.getReceiverId() == null) {
      throw new BadRequestException("Receiver ID cannot be null");
    }
    
    if (notificationDto.getNotificationType() == null) {
      throw new BadRequestException("Notification type cannot be null");
    }
  }

  /**
   * Finds an account by ID.
   *
   * @param accountId The account ID to find
   * @return The found account
   * @throws ResourceNotFoundException if the account is not found
   */
  private EAccountCredentials findAccountById(Long accountId) {
    return accountRepository.findById(accountId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND, accountId)));
  }

  /**
   * Finds a notification by ID.
   *
   * @param notificationId The notification ID to find
   * @return The found notification
   * @throws ResourceNotFoundException if the notification is not found
   */
  private ENotification findNotificationById(Long notificationId) {
    return notificationRepository.findById(notificationId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(NOTIFICATION_NOT_FOUND, notificationId)));
  }

  /**
   * Validates that the user owns the notification.
   *
   * @param notification The notification to check
   * @param userId The user ID to validate ownership
   * @throws BadRequestException if the user doesn't own the notification
   */
  private void validateNotificationOwnership(ENotification notification, Long userId) {
    if (!notification.getReceiver().getId().equals(userId)) {
      throw new BadRequestException("You don't have permission to access this notification");
    }
  }

  /**
   * Builds a notification entity from DTO and receiver.
   *
   * @param notificationDto The notification data
   * @param receiver The receiver account
   * @return The built notification entity
   */
  private ENotification buildNotification(NotificationDto notificationDto, EAccountCredentials receiver) {
    return ENotification.builder()
        .title(notificationDto.getTitle())
        .content(notificationDto.getContent())
        .isRead(false)
        .notificationType(notificationDto.getNotificationType())
        .receiver(receiver)
        .build();
  }

  /**
   * Maps notification entities to view models.
   *
   * @param notifications The notification entities to map
   * @return The mapped view models
   */
  private List<NotificationVm> mapNotificationsToViewModels(List<ENotification> notifications) {
    return notifications.stream()
        .map(this::mapNotificationToViewModel)
        .collect(Collectors.toList());
  }

  /**
   * Maps a single notification entity to view model.
   *
   * @param notification The notification entity to map
   * @return The mapped view model
   */
  private NotificationVm mapNotificationToViewModel(ENotification notification) {
    return NotificationVm.builder()
        .id(notification.getId())
        .title(notification.getTitle())
        .content(notification.getContent())
        .isRead(notification.getIsRead())
        .createdDate(notification.getCreatedDate())
        .build();
  }

  /**
   * Builds a paged response from page data and view models.
   *
   * @param page The page data
   * @param content The view models
   * @return The paged response
   */
  private PagedResponse<NotificationVm> buildPagedResponse(Page<ENotification> page, List<NotificationVm> content) {
    return PagedResponse.<NotificationVm>builder()
        .results(content)
        .totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements())
        .size(page.getSize())
        .page(page.getNumber())
        .build();
  }
}
