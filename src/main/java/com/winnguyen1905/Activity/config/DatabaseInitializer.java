package com.winnguyen1905.Activity.config;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EActivitySchedule;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;

  @Override
  public void run(String... args) throws Exception {
    accountRepository.save(EAccountCredentials.builder().email("12345678").studentCode("12345678").build());
    EActivity activity = EActivity.builder()
        .activityName("Tech Conference 2025 abc")
        .description("A conference on the latest trends in technology.")
        .startDate(Instant.parse("2025-06-15T09:00:00Z"))
        .endDate(Instant.parse("2025-06-15T17:00:00Z"))
        .activityVenue("Tech Park, Building A")
        .activityStatus(ActivityStatus.WAITING_TO_START)
        .capacityLimit(200)
        .capacity(0)
        .activityCategory(ActivityCategory.STUDENT_ORGANIZATION)
        .description("An event featuring keynote speakers and networking opportunities.")
        // .activityImage("https://example.com/images/tech-conference.jpg")
        // .activityLink("https://example.com/tech-conference")
        .attendanceScoreUnit(5)
        // .representativeOrganizerId(101)
        .build();
    activity.setActivitySchedules(List.of(EActivitySchedule.builder()
        .activity(activity)
        .startTime(Instant.parse("2025-05-15T09:30:00Z"))
        .endTime(Instant.parse("2025-06-15T11:30:00Z"))
        .activityDescription("Keynote speech by industry leaders.")
        .status(ScheduleStatus.WAITING_TO_START)
        .location("Main Hall")
        .build(),
        EActivitySchedule.builder()
            .activity(activity)
            .startTime(Instant.parse("2025-06-15T13:00:00Z"))
            .endTime(Instant.parse("2025-06-15T15:00:00Z"))
            .activityDescription("Panel discussion on AI advancements.")
            .status(ScheduleStatus.WAITING_TO_START)
            .location("Conference Room B")
            .build()));
    activityRepository.save(activity);
  }

}
