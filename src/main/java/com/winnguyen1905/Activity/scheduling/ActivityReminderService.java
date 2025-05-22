package com.winnguyen1905.Activity.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityScheduleRepository;
import com.winnguyen1905.Activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.Activity.rest.service.EmailService;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class ActivityReminderService {

  // private final ActivityScheduleRepository activityRepository;
  private final ActivityRepository activityRepository;
  private final ParticipationDetailRepository participationDetailRepository;
  private final EmailService emailService;

  // Runs daily at 8 AM (adjust cron as needed)
  @Scheduled(cron = "0 0 8 20 * ?")
  public void sendActivityReminders() {
    // Calculate the time range: 3 days from now
    Instant now = Instant.now();
    Instant start = now.plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
    Instant end = start.plus(1, ChronoUnit.DAYS);

    // Find activities starting in 3 days
    List<EActivity> activities = activityRepository.findActivitiesStartingInRange(start, end);

    for (EActivity activity : activities) {
      // Get participants for the activity
      List<EParticipationDetail> participationDetails = participationDetailRepository
          .findByActivityId(activity.getId());

      for (EParticipationDetail detail : participationDetails) {
        String participantEmail = detail.getParticipant().getEmail();
        if (participantEmail != null && !participantEmail.isEmpty()) {
          String subject = "Reminder: Upcoming Activity - " + activity.getActivityName();
          String body = String.format(
              "Dear %s,\n\nThis is a reminder for the activity '%s' scheduled on %s at %s.\nDescription: %s\n\nBest regards,\nYour Team",
              detail.getParticipant().getFullName(),
              activity.getActivityName(),
              activity.getStartDate(),
              activity.getVenue(),
              activity.getDescription());
          try {
            emailService.sendEmail(participantEmail, subject, body);
            System.out.println("Sent reminder to: " + participantEmail);
          } catch (Exception e) {
            System.err.println("Failed to send email to " + participantEmail + ": " + e.getMessage());
          }
        }
      }
    }
  }
}
