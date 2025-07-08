package com.winnguyen1905.activity.scheduling;

import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.common.constant.NotificationType;
import com.winnguyen1905.activity.common.constant.ScheduleStatus;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EActivitySchedule;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.activity.persistance.repository.ActivityScheduleRepository;
import com.winnguyen1905.activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.activity.model.dto.NotificationDto;
import com.winnguyen1905.activity.websocket.dto.SocketNotificationDto;
import com.winnguyen1905.activity.rest.service.ActivitySchedulingService;
import com.winnguyen1905.activity.rest.service.EmailService;
import com.winnguyen1905.activity.rest.service.NotificationService;
import com.winnguyen1905.activity.websocket.service.SocketIOService;
import com.winnguyen1905.activity.utils.EmailTemplateUtil;
import com.winnguyen1905.activity.websocket.SocketIoGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ActivitySchedulingServiceImpl implements ActivitySchedulingService {

  private final ActivityRepository activityRepository;
  private final ActivityScheduleRepository activityScheduleRepository;
  private final ParticipationDetailRepository participationDetailRepository;
  private final NotificationService notificationService;
  private final EmailService emailService;
  private final SocketIOService socketIOService;
  private final SocketIoGateway socketIoGateway;

  /**
   * Checks every 5 seconds for activities that have ended (end date < current
   * time)
   * and updates their status to COMPLETED, along with their associated schedules.
   */
  @Scheduled(fixedRate = 5000) // Run every 5 seconds
  public void checkAndUpdateToCompleted() {
    log.debug("Running scheduled task: checkAndUpdateToCompleted");

    Instant now = Instant.now();

    // Find activities that should be marked as COMPLETED:
    // - Status is IN_PROGRESS
    // - endDate is before or equal to now
    List<EActivity> activitiesToUpdate = activityRepository
        .findByStatusAndEndDateBefore(ActivityStatus.IN_PROGRESS, now);

    for (EActivity activity : activitiesToUpdate) {
      // Update status to COMPLETED
      String oldStatus = activity.getStatus().toString();
      activity.setStatus(ActivityStatus.COMPLETED);
      activity = activityRepository.save(activity);

      log.info("Updated activity {} (ID: {}) from {} to COMPLETED",
          activity.getActivityName(), activity.getId(), oldStatus);

      // Update all schedules for this activity to COMPLETED
      List<EActivitySchedule> schedules = activityScheduleRepository.findByActivity(activity);
      for (EActivitySchedule schedule : schedules) {
        if (schedule.getStatus() != ScheduleStatus.COMPLETED) {
          schedule.setStatus(ScheduleStatus.COMPLETED);
          activityScheduleRepository.save(schedule);
          log.debug("Updated schedule ID {} to COMPLETED", schedule.getId());
        }
      }

      // Notify participants about status change using both old and new methods
      notifyStatusChange(activity, oldStatus, ActivityStatus.COMPLETED.toString());
      socketIoGateway.broadcastActivityStatusChange(activity, ActivityStatus.valueOf(oldStatus),
          ActivityStatus.COMPLETED);
    }
  }

  /**
   * Checks every 5 seconds for PUBLISHED and approved activities that should be
   * marked as IN_PROGRESS.
   * This ensures activities transition to IN_PROGRESS status as soon as their
   * start time is reached.
   */
  @Scheduled(fixedRate = 5000) // Run every 5 seconds
  public void checkAndUpdateToInProgress() {
    log.debug("Running scheduled task: checkAndUpdateToInProgress");

    Instant now = Instant.now();

    // Find activities that should be marked as IN_PROGRESS:
    // - Status is PUBLISHED
    // - isApproved is true
    // - startDate is before or equal to now
    // - status is not already IN_PROGRESS
    List<EActivity> activitiesToUpdate = activityRepository
        .findByStatusAndIsApprovedAndStartDateLessThanEqual(
            ActivityStatus.PUBLISHED,
            true,
            now);

    for (EActivity activity : activitiesToUpdate) {
      // Skip if already in progress (just to be safe)
      if (ActivityStatus.IN_PROGRESS.equals(activity.getStatus())) {
        continue;
      }

      // Update status to IN_PROGRESS
      String oldStatus = activity.getStatus().toString();
      activity.setStatus(ActivityStatus.IN_PROGRESS);
      activity = activityRepository.save(activity);

      log.info("Updated activity {} (ID: {}) from {} to IN_PROGRESS",
          activity.getActivityName(), activity.getId(), oldStatus);

      // Notify participants about status change using both old and new methods
      notifyStatusChange(activity, oldStatus, ActivityStatus.IN_PROGRESS.toString());
      socketIoGateway.broadcastActivityStatusChange(activity, ActivityStatus.valueOf(oldStatus),
          ActivityStatus.IN_PROGRESS);
    }
  }

  /**
   * Sends notifications for activities happening today.
   * Runs daily at 7 AM.
   */
  @Scheduled(cron = "0 0 7 * * ?")
  public void sendActivityHappeningTodayNotifications() {
    log.info("Running scheduled task: sendActivityHappeningTodayNotifications");

    Instant now = Instant.now();
    Instant endOfDay = now.plus(24, ChronoUnit.HOURS);

    // Find activities starting today
    List<EActivity> todayActivities = activityRepository.findActivitiesStartingInRange(now, endOfDay);

    for (EActivity activity : todayActivities) {
      // Notify if activity is published (upcoming) or already in progress
      if (!ActivityStatus.PUBLISHED.equals(activity.getStatus()) &&
          !ActivityStatus.IN_PROGRESS.equals(activity.getStatus())) {
        continue;
      }

      List<EParticipationDetail> participants = participationDetailRepository.findByActivityId(activity.getId());

      // Create a socket notification for this activity
      SocketNotificationDto socketNotification = SocketNotificationDto.builder()
          .title("Activity Starting Today!")
          .message("Your activity '" + activity.getActivityName() + "' is starting today!")
          .type(NotificationType.ACTIVITY)
          .activityId(activity.getId())
          .timestamp(Instant.now())
          .daysUntilStart(0L)
          .activityName(activity.getActivityName())
          .activityStartDate(activity.getStartDate())
          .build();

      for (EParticipationDetail detail : participants) {
        EAccountCredentials participant = detail.getParticipant();

        // Send socket notification if user is connected (using both old and new
        // methods)
        socketIOService.sendNotification(participant.getId(), "activity_today", socketNotification);
        socketIoGateway.sendActivityReminder(activity, 0L);

        // Send regular notification
        NotificationDto notification = NotificationDto.builder()
            .title("Activity Today: " + activity.getActivityName())
            .content("Your activity '" + activity.getActivityName() + "' is starting today at " +
                activity.getStartDate())
            .notificationType(NotificationType.ACTIVITY)
            .receiverId(participant.getId())
            .build();

        try {
          notificationService.sendNotification(notification);
        } catch (Exception e) {
          log.error("Failed to send notification to participant {}: {}", participant.getId(), e.getMessage());
        }

        // Send email
        try {
          String emailBody = EmailTemplateUtil.generateActivityReminderBody(activity, participant);
          emailService.sendEmail(participant.getEmail(), "Activity Today: " + activity.getActivityName(),
              emailBody);
          log.info("Sent activity today email to: {}", participant.getEmail());
        } catch (Exception e) {
          log.error("Failed to send email to {}: {}", participant.getEmail(), e.getMessage());
        }
      }
    }
  }

  /**
   * Sends notifications for activities starting in 1 day.
   * Runs daily at 8 AM.
   */
  @Scheduled(cron = "0 0 8 * * ?")
  public void sendActivityOneDayReminderNotifications() {
    log.info("Running scheduled task: sendActivityOneDayReminderNotifications");

    Instant oneDayLater = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant twoDaysLater = oneDayLater.plus(24, ChronoUnit.HOURS);

    // Find activities starting in 1 day
    List<EActivity> oneDayActivities = activityRepository.findActivitiesStartingInRange(oneDayLater, twoDaysLater);

    for (EActivity activity : oneDayActivities) {
      // Only send notifications for confirmed activities
      if (!ActivityStatus.PUBLISHED.equals(activity.getStatus()) &&
          !ActivityStatus.IN_PROGRESS.equals(activity.getStatus())) {
        continue;
      }

      List<EParticipationDetail> participants = participationDetailRepository.findByActivityId(activity.getId());

      // Create a socket notification for this activity
      SocketNotificationDto socketNotification = SocketNotificationDto.builder()
          .title("Activity Starting Tomorrow!")
          .message("Your activity '" + activity.getActivityName() + "' starts tomorrow!")
          .type(NotificationType.ACTIVITY)
          .activityId(activity.getId())
          .timestamp(Instant.now())
          .daysUntilStart(1L)
          .activityName(activity.getActivityName())
          .activityStartDate(activity.getStartDate())
          .build();

      for (EParticipationDetail detail : participants) {
        EAccountCredentials participant = detail.getParticipant();

        // Send socket notification if user is connected (using both old and new
        // methods)
        socketIOService.sendNotification(participant.getId(), "activity_one_day", socketNotification);
        socketIoGateway.sendActivityReminder(activity, 1L);

        // Send regular notification
        NotificationDto notification = NotificationDto.builder()
            .title("Activity Tomorrow: " + activity.getActivityName())
            .content("Your activity '" + activity.getActivityName() + "' is starting tomorrow at " +
                activity.getStartDate())
            .notificationType(NotificationType.ACTIVITY)
            .receiverId(participant.getId())
            .build();

        try {
          notificationService.sendNotification(notification);
        } catch (Exception e) {
          log.error("Failed to send notification to participant {}: {}", participant.getId(), e.getMessage());
        }

        // Send email
        try {
          String emailBody = EmailTemplateUtil.generateActivityReminderBody(activity, participant);
          emailService.sendEmail(participant.getEmail(), "Activity Tomorrow: " + activity.getActivityName(),
              emailBody);
          log.info("Sent activity tomorrow email to: {}", participant.getEmail());
        } catch (Exception e) {
          log.error("Failed to send email to {}: {}", participant.getEmail(), e.getMessage());
        }
      }
    }
  }

  /**
   * Sends notifications for activities starting in 3 days.
   * Runs daily at 9 AM.
   */
  @Scheduled(cron = "0 0 9 * * ?")
  public void sendActivityThreeDayReminderNotifications() {
    log.info("Running scheduled task: sendActivityThreeDayReminderNotifications");

    Instant threeDaysLater = Instant.now().plus(3, ChronoUnit.DAYS);
    Instant fourDaysLater = threeDaysLater.plus(24, ChronoUnit.HOURS);

    // Find activities starting in 3 days
    List<EActivity> threeDayActivities = activityRepository.findActivitiesStartingInRange(threeDaysLater,
        fourDaysLater);

    for (EActivity activity : threeDayActivities) {
      // Only send notifications for PUBLISHED activities
      if (!ActivityStatus.PUBLISHED.equals(activity.getStatus())) {
        continue;
      }

      List<EParticipationDetail> participants = participationDetailRepository.findByActivityId(activity.getId());

      // Create a socket notification for this activity
      SocketNotificationDto socketNotification = SocketNotificationDto.builder()
          .title("Activity in 3 Days!")
          .message("Your activity '" + activity.getActivityName() + "' starts in 3 days!")
          .type(NotificationType.ACTIVITY)
          .activityId(activity.getId())
          .timestamp(Instant.now())
          .daysUntilStart(3L)
          .activityName(activity.getActivityName())
          .activityStartDate(activity.getStartDate())
          .build();

      for (EParticipationDetail detail : participants) {
        EAccountCredentials participant = detail.getParticipant();

        // Send socket notification if user is connected (using both old and new
        // methods)
        socketIOService.sendNotification(participant.getId(), "activity_three_days", socketNotification);
        socketIoGateway.sendActivityReminder(activity, 3L);

        // Send regular notification
        NotificationDto notification = NotificationDto.builder()
            .title("Activity in 3 Days: " + activity.getActivityName())
            .content("Your activity '" + activity.getActivityName() + "' is starting in 3 days on " +
                activity.getStartDate())
            .notificationType(NotificationType.ACTIVITY)
            .receiverId(participant.getId())
            .build();

        try {
          notificationService.sendNotification(notification);
        } catch (Exception e) {
          log.error("Failed to send notification to participant {}: {}", participant.getId(), e.getMessage());
        }

        // Send email
        try {
          String emailBody = EmailTemplateUtil.generateActivityReminderBody(activity, participant);
          emailService.sendEmail(participant.getEmail(), "Activity in 3 Days: " + activity.getActivityName(),
              emailBody);
          log.info("Sent activity 3-day reminder email to: {}", participant.getEmail());
        } catch (Exception e) {
          log.error("Failed to send email to {}: {}", participant.getEmail(), e.getMessage());
        }
      }
    }
  }

  /**
   * Sends notifications for activities starting in the next 24 hours.
   * Runs daily at 10 AM.
   */
  @Override
  @Scheduled(cron = "0 0 10 * * ?")
  public void sendUpcomingActivityNotifications() {
    log.info("Running scheduled task: sendUpcomingActivityNotifications");

    Instant now = Instant.now();
    Instant oneDayLater = now.plus(24, ChronoUnit.HOURS);

    // Find activities starting in the next 24 hours
    List<EActivity> upcomingActivities = activityRepository.findActivitiesStartingInRange(now, oneDayLater);

    for (EActivity activity : upcomingActivities) {
      // Only send notifications for confirmed activities
      if (!ActivityStatus.PUBLISHED.equals(activity.getStatus()) &&
          !ActivityStatus.IN_PROGRESS.equals(activity.getStatus())) {
        continue;
      }

      List<EParticipationDetail> participants = participationDetailRepository.findByActivityId(activity.getId());

      for (EParticipationDetail detail : participants) {
        EAccountCredentials participant = detail.getParticipant();

        // Send notification
        NotificationDto notification = NotificationDto.builder()
            .title("Activity Reminder: " + activity.getActivityName())
            .content("Your activity '" + activity.getActivityName() + "' is starting soon on "
                + activity.getStartDate())
            .notificationType(NotificationType.ACTIVITY)
            .receiverId(participant.getId())
            .build();

        try {
          notificationService.sendNotification(notification);
        } catch (Exception e) {
          log.error("Failed to send notification to participant {}: {}", participant.getId(), e.getMessage());
        }

        // Send email
        try {
          String emailBody = EmailTemplateUtil.generateActivityReminderBody(activity, participant);
          emailService.sendEmail(participant.getEmail(), "Activity Reminder: " + activity.getActivityName(),
              emailBody);
          log.info("Sent activity reminder email to: {}", participant.getEmail());
        } catch (Exception e) {
          log.error("Failed to send email to {}: {}", participant.getEmail(), e.getMessage());
        }
      }
    }
  }

  /**
   * Sends notifications for activity schedules starting in the next 24 hours.
   * Runs daily at 10 AM.
   */
  @Override
  @Scheduled(cron = "0 0 11 * * ?")
  public void sendUpcomingScheduleNotifications() {
    log.info("Running scheduled task: sendUpcomingScheduleNotifications");

    Instant now = Instant.now();
    Instant oneDayLater = now.plus(24, ChronoUnit.HOURS);

    // Find schedules starting in the next 24 hours
    List<EActivitySchedule> upcomingSchedules = activityScheduleRepository.findSchedulesStartingBetween(now,
        oneDayLater);

    for (EActivitySchedule schedule : upcomingSchedules) {
      // Only send notifications for confirmed schedules
      if (!ScheduleStatus.WAITING_TO_START.equals(schedule.getStatus()) &&
          !ScheduleStatus.IN_PROGRESS.equals(schedule.getStatus())) {
        continue;
      }

      EActivity activity = schedule.getActivity();
      List<EParticipationDetail> participants = participationDetailRepository.findByActivityId(activity.getId());

      for (EParticipationDetail detail : participants) {
        EAccountCredentials participant = detail.getParticipant();

        // Send notification
        NotificationDto notification = NotificationDto.builder()
            .title("Schedule Reminder: " + activity.getActivityName())
            .content("A schedule for '" + activity.getActivityName() + "' is starting soon at "
                + schedule.getStartTime())
            .notificationType(NotificationType.ACTIVITY)
            .receiverId(participant.getId())
            .build();

        try {
          notificationService.sendNotification(notification);
        } catch (Exception e) {
          log.error("Failed to send notification to participant {}: {}", participant.getId(), e.getMessage());
        }

        // Send socket notification
        SocketNotificationDto socketNotification = SocketNotificationDto.builder()
            .title("Schedule Reminder!")
            .message("A schedule for '" + activity.getActivityName() + "' is starting soon at "
                + schedule.getStartTime())
            .type(NotificationType.ACTIVITY)
            .activityId(activity.getId())
            .timestamp(Instant.now())
            .activityName(activity.getActivityName())
            .activityStartDate(schedule.getStartTime())
            .build();

        socketIOService.sendNotification(participant.getId(), "schedule_reminder", socketNotification);

        // Send email
        try {
          String emailBody = EmailTemplateUtil.generateScheduleReminderBody(schedule, participant);
          emailService.sendEmail(participant.getEmail(), "Schedule Reminder: " + activity.getActivityName(),
              emailBody);
          log.info("Sent schedule reminder email to: {}", participant.getEmail());
        } catch (Exception e) {
          log.error("Failed to send email to {}: {}", participant.getEmail(), e.getMessage());
        }
      }
    }
  }

  /**
   * Updates activity statuses based on current date and deadlines.
   * Runs daily at midnight.
   */
  @Override
  @Scheduled(cron = "0 0 0 * * ?")
  public void updateActivityStatuses() {
    log.info("Running scheduled task: updateActivityStatuses");
    Instant now = Instant.now();

    // Find activities that have ended but still marked as ongoing
    List<EActivity> endedActivities = activityRepository.findByStatusAndEndDateBefore(ActivityStatus.IN_PROGRESS,
        now);
    for (EActivity activity : endedActivities) {
      // Update to COMPLETED status
      String oldStatus = activity.getStatus().toString();
      activity.setStatus(ActivityStatus.COMPLETED);
      activityRepository.save(activity);

      // Notify participants about status change
      notifyStatusChange(activity, oldStatus, ActivityStatus.COMPLETED.toString());
    }

    // Find published activities whose start date has passed but not yet marked as
    // in progress
    List<EActivity> startedActivities = activityRepository.findByStatusAndStartDateBefore(ActivityStatus.PUBLISHED,
        now);
    for (EActivity activity : startedActivities) {
      // Update to ONGOING status
      String oldStatus = activity.getStatus().toString();
      activity.setStatus(ActivityStatus.IN_PROGRESS);
      activityRepository.save(activity);

      // Notify participants about status change
      notifyStatusChange(activity, oldStatus, ActivityStatus.IN_PROGRESS.toString());
    }

    // Find pending activities whose registration deadline has passed
    List<EActivity> expiredRegistrations = activityRepository
        .findByStatusAndRegistrationDeadlineBefore(ActivityStatus.PENDING, now);
    for (EActivity activity : expiredRegistrations) {
      // Check if minimum participants reached
      if (activity.getCurrentParticipants() >= activity.getCapacityLimit() * 0.3) { // 30% capacity as minimum
                                                                                    // threshold
        // Update to CONFIRMED status
        String oldStatus = activity.getStatus().toString();
        activity.setStatus(ActivityStatus.PUBLISHED);
        activityRepository.save(activity);

        // Notify participants about status change
        notifyStatusChange(activity, oldStatus, ActivityStatus.PUBLISHED.toString());
      } else {
        // Update to CANCELED status (not enough participants)
        String oldStatus = activity.getStatus().toString();
        activity.setStatus(ActivityStatus.CANCELLED);
        activityRepository.save(activity);

        // Notify participants about status change
        notifyStatusChange(activity, oldStatus, ActivityStatus.CANCELLED.toString());
      }
    }
  }

  /**
   * Sends reminders about approaching registration deadlines.
   * Runs daily at 8 AM.
   */
  @Override
  @Scheduled(cron = "0 0 8 * * ?")
  public void sendRegistrationDeadlineReminders() {
    log.info("Running scheduled task: sendRegistrationDeadlineReminders");

    Instant now = Instant.now();
    Instant twoDaysLater = now.plus(2, ChronoUnit.DAYS);

    // Find activities with registration deadlines approaching within 2 days
    List<EActivity> activities = activityRepository.findByRegistrationDeadlineBetween(now, twoDaysLater);

    for (EActivity activity : activities) {
      // Only send reminders for pending activities
      if (!ActivityStatus.PENDING.equals(activity.getStatus())) {
        continue;
      }

      List<EParticipationDetail> participants = participationDetailRepository.findByActivityId(activity.getId());

      for (EParticipationDetail detail : participants) {
        EAccountCredentials participant = detail.getParticipant();

        // Send notification
        NotificationDto notification = NotificationDto.builder()
            .title("Registration Deadline: " + activity.getActivityName())
            .content("Registration for '" + activity.getActivityName() + "' closes on "
                + activity.getRegistrationDeadline())
            .notificationType(NotificationType.ACTIVITY)
            .receiverId(participant.getId())
            .build();

        try {
          notificationService.sendNotification(notification);
        } catch (Exception e) {
          log.error("Failed to send notification to participant {}: {}", participant.getId(), e.getMessage());
        }

        // Send email
        try {
          String emailBody = EmailTemplateUtil.generateRegistrationDeadlineBody(activity, participant);
          emailService.sendEmail(participant.getEmail(),
              "Registration Deadline: " + activity.getActivityName(), emailBody);
          log.info("Sent registration deadline email to: {}", participant.getEmail());
        } catch (Exception e) {
          log.error("Failed to send email to {}: {}", participant.getEmail(), e.getMessage());
        }
      }
    }
  }

  /**
   * Helper method to notify all participants about activity status changes.
   */
  private void notifyStatusChange(EActivity activity, String oldStatus, String newStatus) {
    List<EParticipationDetail> participants = participationDetailRepository.findByActivityId(activity.getId());

    for (EParticipationDetail detail : participants) {
      EAccountCredentials participant = detail.getParticipant();

      // Send notification
      NotificationDto notification = NotificationDto.builder()
          .title("Activity Status Update: " + activity.getActivityName())
          .content("Activity '" + activity.getActivityName() + "' status changed from " + oldStatus + " to "
              + newStatus)
          .notificationType(NotificationType.ACTIVITY)
          .receiverId(participant.getId())
          .build();

      try {
        notificationService.sendNotification(notification);
      } catch (Exception e) {
        log.error("Failed to send notification to participant {}: {}", participant.getId(), e.getMessage());
      }

      // Send socket notification
      SocketNotificationDto socketNotification = SocketNotificationDto.builder()
          .title("Activity Status Change")
          .message("Activity '" + activity.getActivityName() + "' status changed from " + oldStatus + " to "
              + newStatus)
          .type(NotificationType.ACTIVITY)
          .activityId(activity.getId())
          .timestamp(Instant.now())
          .activityName(activity.getActivityName())
          .activityStartDate(activity.getStartDate())
          .build();

      socketIOService.sendNotification(participant.getId(), "activity_status_change", socketNotification);

      // Send email
      try {
        String emailBody = EmailTemplateUtil.generateActivityStatusChangeBody(activity, participant, oldStatus,
            newStatus);
        emailService.sendEmail(participant.getEmail(), "Activity Status Update: " + activity.getActivityName(),
            emailBody);
        log.info("Sent status change email to: {}", participant.getEmail());
      } catch (Exception e) {
        log.error("Failed to send email to {}: {}", participant.getEmail(), e.getMessage());
      }
    }
  }
}
