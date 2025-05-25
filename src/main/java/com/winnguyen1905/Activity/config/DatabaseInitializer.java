package com.winnguyen1905.Activity.config;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.OrganizationType;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EActivitySchedule;
import com.winnguyen1905.Activity.persistance.entity.EClass;
import com.winnguyen1905.Activity.persistance.entity.EOrganization;
import com.winnguyen1905.Activity.persistance.entity.EStudentSemesterDetail;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.ClassRepository;
import com.winnguyen1905.Activity.persistance.repository.OrganizationRepository;
import com.winnguyen1905.Activity.persistance.repository.StudentSemesterDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

  private final ClassRepository classRepository;
  private final PasswordEncoder passwordEncoder;
  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final OrganizationRepository organizationRepository;
  private final StudentSemesterDetailRepository semesterDetailRepository;

  @Override
  public void run(String... args) throws Exception {
    EAccountCredentials account = EAccountCredentials.builder().isActive(true).fullName("Nguyên Thắng Lợi 1")
        .email("winnguyen1905.work@gmail.com")
        .studentCode("1")
        .role(AccountRole.STUDENT).password(passwordEncoder.encode("1")).build();
    EAccountCredentials account2 = EAccountCredentials.builder().isActive(true).fullName("Nguyên Thắng Lợi 2")
        .email("winnguyen1905.work@gmail.com")
        .studentCode("2")
        .role(AccountRole.ADMIN).password(passwordEncoder.encode("2")).build();

    EOrganization organization = EOrganization.builder()
        .name("Student Technology Club")
        .phone("0123456789")
        .email("techclub@university.edu")
        .type(OrganizationType.CLUB)
        .build();
    EAccountCredentials account3 = EAccountCredentials.builder().isActive(true).fullName("Nguyên Thắng Lợi 3")
        .email("winnguyen1905.work@gmail.com")
        .studentCode("3")
        .role(AccountRole.ORGANIZATION).password(passwordEncoder.encode("3"))
        .organization(organization)
        .build();
    organization.setAccount(account3);

    accountRepository.saveAll(List.of(account, account2, account3));
    EActivity activity = EActivity.builder()
        .activityName("Tech Conference 2025")
        .description("A comprehensive tech conference featuring industry experts")
        .startDate(Instant.parse("2025-08-19T09:00:00Z"))
        .endDate(Instant.parse("2025-08-20T17:00:00Z"))
        .venue("Tech Innovation Center")
        .capacityLimit(200)
        .status(ActivityStatus.PUBLISHED)
        .activityCategory(ActivityCategory.STUDENT_ORGANIZATION)
        .imageUrl("https://example.com/images/techconf2025.jpg")
        .shortDescription("Join us for the biggest tech event of 2025")
        .tags(List.of("technology,innovation,networking"))
        .currentParticipants(0)
        .address("123 Tech Street, Innovation District")
        .latitude(10.762622)
        .longitude(106.660172)
        .fee(50.00)
        .isFeatured(false)
        .isApproved(false)
        .likes(0)
        .registrationDeadline(Instant.parse("2025-08-01T23:59:59Z"))
        .attendanceScoreUnit(5)
        .organization(organization)
        .createdById(1L)
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

    List<EStudentSemesterDetail> semesterDetails = List.of(
        EStudentSemesterDetail.builder()
            .semesterNumber(1)
            .semesterYear("2025-2026")
            .gpa(3.5f)
            .attendanceScore(80)
            .startDate(Instant.parse("2025-02-01T00:00:00Z"))
            .endDate(Instant.parse("2025-12-15T23:59:59Z"))
            .student(account)
            .build(),
        EStudentSemesterDetail.builder()
            .gpa(3.7f)
            .semesterYear("2025-2026")
            .attendanceScore(83)
            .student(account)
            .semesterNumber(2)
            .startDate(Instant.parse("2026-01-10T00:00:00Z"))
            .endDate(Instant.parse("2026-05-15T23:59:59Z"))
            .build());

    EClass studentClass = EClass.builder()
        .className("CS101")
        .academicYear(2025)
        .startDate(ZonedDateTime.parse("2025-08-01T00:00:00Z").toLocalDate())
        .endDate(ZonedDateTime.parse("2026-05-15T23:59:59Z").toLocalDate())
        .department("Computer Science")
        .capacity(50)
        .status(com.winnguyen1905.Activity.common.constant.ClassStatus.ACTIVE)
        .students(List.of(account))
        .build();

    classRepository.save(studentClass);
    semesterDetailRepository.saveAll(semesterDetails);
    activityRepository.save(activity);
  }

}
