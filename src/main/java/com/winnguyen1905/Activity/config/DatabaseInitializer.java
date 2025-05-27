package com.winnguyen1905.Activity.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.ClassStatus;
import com.winnguyen1905.Activity.common.constant.NotificationType;
import com.winnguyen1905.Activity.common.constant.OrganizationType;
import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EActivitySchedule;
import com.winnguyen1905.Activity.persistance.entity.EClass;
import com.winnguyen1905.Activity.persistance.entity.EConfirmation;
import com.winnguyen1905.Activity.persistance.entity.EDepartment;
import com.winnguyen1905.Activity.persistance.entity.EFacultyAdvisor;
import com.winnguyen1905.Activity.persistance.entity.EFeedback;
import com.winnguyen1905.Activity.persistance.entity.ENotification;
import com.winnguyen1905.Activity.persistance.entity.EOrganization;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.Activity.persistance.entity.EReport;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityRepository;
import com.winnguyen1905.Activity.persistance.repository.ActivityScheduleRepository;
import com.winnguyen1905.Activity.persistance.repository.ClassRepository;
import com.winnguyen1905.Activity.persistance.repository.ConfirmationRepository;
import com.winnguyen1905.Activity.persistance.repository.FeedbackRepository;
import com.winnguyen1905.Activity.persistance.repository.NotificationRepository;
import com.winnguyen1905.Activity.persistance.repository.OrganizationRepository;
import com.winnguyen1905.Activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.Activity.persistance.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

  private final ClassRepository classRepository;
  private final PasswordEncoder passwordEncoder;
  private final AccountRepository accountRepository;
  private final ActivityRepository activityRepository;
  private final OrganizationRepository organizationRepository;
  private final ParticipationDetailRepository participationDetailRepository;
  private final FeedbackRepository feedbackRepository;
  private final ActivityScheduleRepository activityScheduleRepository;
  private final NotificationRepository notificationRepository;
  private final ReportRepository reportRepository;
  private final ConfirmationRepository confirmationRepository;

  private final Random random = new Random();

  @Override
  public void run(String... args) throws Exception {
    // Create student accounts and admin accounts
    List<EAccountCredentials> accounts = createAccounts();
    accountRepository.saveAll(accounts);

    // Create classes and associate students
    // List<EClass> classes = createClasses(departments,
    // getStudentAccounts(accounts));
    // classRepository.saveAll(classes);

    // Create semester details for students

    // Create organizations and their accounts
    EAccountCredentials adminAccount = accounts.stream()
        .filter(account -> account.getId() == 3)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Admin account not found"));
    List<EOrganization> organizations = new ArrayList<>();
    organizations.add(adminAccount.getOrganization()); // Add admin's organization to the list
    // List<EAccountCredentials> orgAccounts = createOrganizationAccounts(organizations);
    // accountRepository.saveAll(orgAccounts);
    // organizationRepository.saveAll(organizations);

    List<EActivity> activities = createActivities(organizations);
    activityRepository.saveAll(activities);

    // Create activity schedules
    List<EActivitySchedule> schedules = createActivitySchedules(activities);
    activityScheduleRepository.saveAll(schedules);

    // Create participation details (students participating in activities)
    List<EParticipationDetail> participations = createParticipationDetails(activities,
        getStudentAccounts(accounts));
    participationDetailRepository.saveAll(participations);

    // Update activity participant counts
    updateActivityParticipantCounts(activities, participations);
    activityRepository.saveAll(activities);

    // Create feedbacks from participants
    List<EFeedback> feedbacks = createFeedbacks(participations);
    feedbackRepository.saveAll(feedbacks);

    // Create notifications
    List<ENotification> notifications = createNotifications(accounts,
        activities);
    notificationRepository.saveAll(notifications);

    // Create reports
    List<EReport> reports = createReports(activities,
        getStudentAccounts(accounts));
    reportRepository.saveAll(reports);

    System.out.println("Database seeding completed successfully!");
  }

  private List<EAccountCredentials> createAccounts() {
    List<EAccountCredentials> accounts = new ArrayList<>();
    // Admin accounts
    accounts.add(EAccountCredentials.builder().isActive(true)
        .fullName("Admin User")
        .email("admin@university.edu")
        .studentCode("2")
        .role(AccountRole.ADMIN)
        .password(passwordEncoder.encode("1"))
        .build());

    accounts.add(EAccountCredentials.builder().isActive(true)
        .fullName("Student User")
        .email("student@university.edu")
        .studentCode("1")
        .role(AccountRole.STUDENT)
        .password(passwordEncoder.encode("1"))
        .build());

    EAccountCredentials organizationAccount = EAccountCredentials.builder().isActive(true)
        .fullName("Organization User")
        .email("organization@university.edu")
        .studentCode("3")
        .role(AccountRole.ORGANIZATION)
        .password(passwordEncoder.encode("1"))
        .build();
    accounts.add(organizationAccount);

    EOrganization adminOrg = EOrganization.builder()
        .name("University Admin")
        .description("Administrative organization for university management")
        .phone("0123456789")
        .email("admin@university.edu")
        .type(OrganizationType.UNIVERSITY)
        .account(organizationAccount)
        .build();
    organizationAccount.setOrganization(adminOrg);

    accountRepository.saveAll(accounts);
    // Create 20 student accounts
    String[] firstNames = { "John", "Emma", "Michael", "Sophia", "James", "Olivia", "William", "Ava", "Alexander",
        "Isabella" };
    String[] lastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia",
        "Rodriguez", "Wilson" };

    for (int i = 1; i <= 20; i++) {
      String firstName = firstNames[random.nextInt(firstNames.length)];
      String lastName = lastNames[random.nextInt(lastNames.length)];
      String fullName = firstName + " " + lastName;
      String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + i + "@university.edu";
      String studentCode = "S" + String.format("%03d", i);

      accounts.add(EAccountCredentials.builder().isActive(true)
          .fullName(fullName)
          .email(email)
          .studentCode(studentCode)
          .role(AccountRole.STUDENT)
          .password(passwordEncoder.encode(studentCode))
          .build());
    }

    return accounts;
  }

  private List<EAccountCredentials> getStudentAccounts(List<EAccountCredentials> allAccounts) {
    return allAccounts.stream()
        .filter(account -> account.getRole() == AccountRole.STUDENT)
        .toList();
  }

  private List<EOrganization> createOrganizations() {
    List<EOrganization> organizations = new ArrayList<>();

    // Create 8 diverse organizations
    String[][] orgDetails = {
        { "Student Technology Club", "A club focused on technology and innovation", "0123456789",
            "tech.club@university.edu", OrganizationType.CLUB.toString() },
        { "Environmental Society", "Working for campus sustainability", "0123456790",
            "eco.society@university.edu", OrganizationType.CLUB.toString() },
        { "Business Leaders Association", "Networking for future entrepreneurs", "0123456791",
            "business.leaders@university.edu", OrganizationType.UNIVERSITY.toString() },
        { "Arts & Culture Committee", "Promoting arts on campus", "0123456792", "arts.committee@university.edu",
            OrganizationType.UNIVERSITY.toString() },
        { "Sports Federation", "Coordinating university sports", "0123456793", "sports.fed@university.edu",
            OrganizationType.COMPANY.toString() },
        { "Science Innovation Hub", "Supporting scientific projects", "0123456794", "sci.hub@university.edu",
            OrganizationType.COMPANY.toString() },
        { "International Students Union", "Supporting international students", "0123456795",
            "int.students@university.edu", OrganizationType.UNIVERSITY.toString() },
        { "Volunteering Network", "Coordinating volunteer opportunities", "0123456796",
            "volunteers@university.edu", OrganizationType.CLUB.toString() }
    };

    for (String[] details : orgDetails) {
      organizations.add(EOrganization.builder()
          .name(details[0])
          .description(details[1])
          .phone(details[2])
          .email(details[3])
          .type(OrganizationType.valueOf(details[4]))
          .build());
    }

    return organizations;
  }

  private List<EAccountCredentials> createOrganizationAccounts(List<EOrganization> organizations) {
    List<EAccountCredentials> orgAccounts = new ArrayList<>();

    for (int i = 0; i < organizations.size(); i++) {
      EOrganization org = organizations.get(i);
      String orgName = org.getName().replaceAll("\\s+", "");
      String username = "org" + (i + 1);

      EAccountCredentials account = EAccountCredentials.builder()
          .isActive(true)
          .fullName(org.getName() + " Admin")
          .email(org.getEmail())
          .studentCode(username)
          .role(AccountRole.ORGANIZATION)
          .password(passwordEncoder.encode(username))
          .organization(org)
          .build();

      org.setAccount(account);
      orgAccounts.add(account);
    }

    return orgAccounts;
  }

  private List<EActivity> createActivities(List<EOrganization> organizations) {
    List<EActivity> activities = new ArrayList<>();

    // Activity details: name, description, short description, category, venue,
    // capacity, fee, featured
    Object[][] activityDetails = {
        { "Tech Conference 2025", "A comprehensive tech conference featuring industry experts",
            "Join us for the biggest tech event of 2025", ActivityCategory.STUDENT_ORGANIZATION,
            "Tech Innovation Center", 200, 50.0, true },
        { "Environmental Cleanup", "Join our effort to clean up the university campus",
            "Make our campus greener", ActivityCategory.THIRD_PARTY, "University Campus", 100, 0.0,
            false },
        { "Business Leadership Workshop", "Learn essential leadership skills from industry professionals",
            "Develop your leadership potential", ActivityCategory.THIRD_PARTY, "Business School Auditorium",
            50, 25.0, true },
        { "Cultural Diversity Festival", "Celebrate the diversity of cultures on campus",
            "Experience global cultures", ActivityCategory.UNIVERSITY, "University Square", 300, 10.0,
            true },
        { "Coding Bootcamp", "Intensive coding workshop for beginners and intermediate developers",
            "Build your coding skills", ActivityCategory.UNIVERSITY, "Computer Science Building", 30, 75.0,
            false },
        { "Sports Tournament", "Inter-departmental sports competition", "Compete for the university cup",
            ActivityCategory.UNIVERSITY, "University Sports Complex", 200, 15.0, false },
        { "Research Symposium", "Showcase of student and faculty research projects",
            "Discover innovative research", ActivityCategory.THIRD_PARTY, "Science Building Auditorium",
            150,
            5.0, true },
        { "Career Fair 2025", "Connect with potential employers from various industries", "Find your dream job",
            ActivityCategory.THIRD_PARTY, "University Convention Center", 500, 0.0, true },
        { "Music Concert", "Performance by university bands and local artists", "Enjoy live music",
            ActivityCategory.THIRD_PARTY, "University Amphitheater", 400, 20.0, false },
        { "Health and Wellness Expo", "Learn about maintaining physical and mental health",
            "Prioritize your wellbeing", ActivityCategory.STUDENT_ORGANIZATION, "University Health Center",
            200, 0.0,
            false },
        { "Entrepreneurship Hackathon", "48-hour competition to develop innovative business ideas",
            "Launch your startup", ActivityCategory.UNIVERSITY, "Innovation Hub", 80, 30.0, true },
        { "Volunteer Orientation", "Training session for new community service volunteers",
            "Start your volunteering journey", ActivityCategory.STUDENT_ORGANIZATION, "Community Center",
            50, 0.0,
            false },
        { "Alumni Networking Event", "Connect with university alumni from various fields",
            "Expand your professional network", ActivityCategory.UNIVERSITY, "Alumni Center", 120, 15.0,
            false },
        { "Art Exhibition", "Showcase of student artwork from various disciplines",
            "Experience student creativity", ActivityCategory.THIRD_PARTY, "University Art Gallery", 100,
            5.0, false },
        { "Debate Competition", "Inter-university debate on current social issues",
            "Witness intellectual discourse", ActivityCategory.THIRD_PARTY, "Main Auditorium", 150, 10.0,
            false }
    };

    // Create activities with various statuses
    ActivityStatus[] statuses = ActivityStatus.values();

    for (int i = 0; i < activityDetails.length; i++) {
      Object[] details = activityDetails[i];
      EOrganization org = organizations.getFirst();

      ActivityStatus status = statuses[i % statuses.length];

      // Create current date and manipulate for different date ranges
      Instant now = Instant.parse("2025-05-26T09:00:00Z"); // Current date for reference

      // Different date patterns based on index
      Instant startDate, endDate, registrationDeadline;
      if (i % 3 == 0) { // Future activities
        startDate = now.plusSeconds(60 * 60 * 24 * (15 + i)); // 15+ days in future
        endDate = startDate.plusSeconds(60 * 60 * 24 * 2); // 2-day event
        registrationDeadline = startDate.minusSeconds(60 * 60 * 24 * 5); // 5 days before start
      } else if (i % 3 == 1) { // Ongoing activities
        startDate = now.minusSeconds(60 * 60 * 24 * 1); // Started yesterday
        endDate = now.plusSeconds(60 * 60 * 24 * 3); // Ends in 3 days
        registrationDeadline = now.minusSeconds(60 * 60 * 24 * 7); // Deadline was a week ago
      } else { // Past activities
        startDate = now.minusSeconds(60 * 60 * 24 * (20 + i)); // 20+ days ago
        endDate = startDate.plusSeconds(60 * 60 * 24 * 2); // 2-day event
        registrationDeadline = startDate.minusSeconds(60 * 60 * 24 * 5); // 5 days before start
      }

      boolean isApproved = status != ActivityStatus.PENDING;

      EActivity activity = EActivity.builder()
          .activityName((String) details[0])
          .description((String) details[1])
          .shortDescription((String) details[2])
          .activityCategory((ActivityCategory) details[3])
          .venue((String) details[4])
          .capacityLimit((Integer) details[5])
          .fee((Double) details[6])
          .isFeatured((Boolean) details[7])
          .startDate(startDate)
          .endDate(endDate)
          .registrationDeadline(registrationDeadline)
          .status(status)
          .currentParticipants(0) // Will be updated later
          .tags(generateTags((ActivityCategory) details[3]))
          .address(generateAddress((String) details[4]))
          .latitude(10.762622 + (random.nextDouble() * 0.02 - 0.01)) // Slight variations in coordinates
          .longitude(106.660172 + (random.nextDouble() * 0.02 - 0.01))
          .imageUrl("https://example.com/images/activity" + i + ".jpg")
          .isApproved(isApproved)
          .likes(random.nextInt(50))
          .attendanceScoreUnit(5 + random.nextInt(6)) // 5-10 points
          .organization(org)
          .createdById(org.getAccount().getId())
          .build();

      activities.add(activity);
    }

    return activities;
  }

  private List<String> generateTags(ActivityCategory category) {
    List<String> baseTags = new ArrayList<>();

    // Category-specific tags
    switch (category) {
      case STUDENT_ORGANIZATION:
        baseTags.addAll(Arrays.asList("conference", "networking", "professional"));
        break;
      case UNIVERSITY:
        baseTags.addAll(Arrays.asList("volunteer", "community", "service"));
        break;
      case THIRD_PARTY:
        baseTags.addAll(Arrays.asList("workshop", "skills", "training"));
        break;
      // case THIRD_PARTY:
      // baseTags.addAll(Arrays.asList("culture", "diversity", "festival"));
      // break;
      // case UNIVERSITY:
      // baseTags.addAll(Arrays.asList("learning", "skills", "development"));
      // break;
      // case STUDENT_ORGANIZATION:
      // baseTags.addAll(Arrays.asList("sports", "competition", "fitness"));
      // break;
      default:
        baseTags.addAll(Arrays.asList("university", "education", "campus"));
    }

    // Add some random common tags
    String[] commonTags = { "students", "university", "education", "learning", "campus", "social" };
    baseTags.add(commonTags[random.nextInt(commonTags.length)]);

    return baseTags;
  }

  private String generateAddress(String venue) {
    String[] streets = { "University Ave", "Campus Road", "College Street", "Academic Drive",
        "Research Boulevard" };
    String[] areas = { "University District", "Campus Area", "College Town", "University Heights",
        "Academic Village" };

    return venue + ", " + random.nextInt(100) + " " + streets[random.nextInt(streets.length)] + ", "
        + areas[random.nextInt(areas.length)];
  }

  private List<EActivitySchedule> createActivitySchedules(List<EActivity> activities) {
    List<EActivitySchedule> schedules = new ArrayList<>();

    for (EActivity activity : activities) {
      // Number of schedules for this activity (1-3)
      int numSchedules = 1 + random.nextInt(3);

      for (int i = 0; i < numSchedules; i++) {
        // Calculate time for this schedule segment
        Instant activityStart = activity.getStartDate();
        Instant activityEnd = activity.getEndDate();
        long durationSeconds = activityEnd.getEpochSecond() - activityStart.getEpochSecond();
        long segmentDuration = durationSeconds / numSchedules;

        Instant segmentStart = activityStart.plusSeconds(i * segmentDuration);
        Instant segmentEnd = segmentStart.plusSeconds(segmentDuration);

        // Determine schedule status based on dates
        ScheduleStatus status;
        Instant now = Instant.parse("2025-05-26T09:00:00Z"); // Current date

        if (segmentEnd.isBefore(now)) {
          status = ScheduleStatus.FINISHED;
        } else if (segmentStart.isBefore(now) && segmentEnd.isAfter(now)) {
          status = ScheduleStatus.ONGOING;
        } else {
          status = ScheduleStatus.WAITING_TO_START;
        }

        String[] locations = { "Main Hall", "Conference Room A", "Conference Room B", "Auditorium",
            "Workshop Space",
            "Lecture Theater", "Meeting Room 1", "Seminar Room", "Training Lab", "Exhibition Hall" };

        String[] descriptions = {
            "Keynote presentation by industry experts",
            "Panel discussion on current trends",
            "Interactive workshop session",
            "Networking and refreshments",
            "Group activities and team building",
            "Q&A session with speakers",
            "Hands-on training exercises",
            "Project showcase and demonstrations",
            "Breakout discussions in small groups",
            "Award ceremony and closing remarks"
        };

        EActivitySchedule schedule = EActivitySchedule.builder()
            .activity(activity)
            .startTime(segmentStart)
            .endTime(segmentEnd)
            .activityDescription(descriptions[random.nextInt(descriptions.length)])
            .status(status)
            .location(locations[random.nextInt(locations.length)])
            .build();

        schedules.add(schedule);
      }
    }

    return schedules;
  }

  private List<EParticipationDetail> createParticipationDetails(List<EActivity> activities,
      List<EAccountCredentials> students) {
    List<EParticipationDetail> participations = new ArrayList<>();

    for (EActivity activity : activities) {
      // Determine number of participants based on capacity (30-80% of capacity)
      int capacity = activity.getCapacityLimit();
      int participantCount = (int) (capacity * (0.3 + random.nextDouble() * 0.5));

      // Ensure we don't exceed available students
      participantCount = Math.min(participantCount, students.size());

      // Randomly select students to participate
      List<EAccountCredentials> shuffledStudents = new ArrayList<>(students);
      java.util.Collections.shuffle(shuffledStudents);

      for (int i = 0; i < participantCount; i++) {
        EAccountCredentials student = shuffledStudents.get(i);

        // Determine participation role (80% participants, 20% volunteers)
        ParticipationRole role = random.nextDouble() < 0.8 ? ParticipationRole.PARTICIPANT
            : ParticipationRole.CONTRIBUTOR;

        // Determine participation status based on activity status
        ParticipationStatus status = ParticipationStatus.UNVERIFIED;
        if (activity.getStatus() == ActivityStatus.COMPLETED) {
          // For completed activities, mostly ATTENDED with some ABSENT
          status = random.nextDouble() < 0.9 ? ParticipationStatus.UNVERIFIED
              : ParticipationStatus.UNVERIFIED;
        } else if (activity.getStatus() == ActivityStatus.CANCELLED) {
          status = ParticipationStatus.REJECTED;
        } else if (activity.getStatus() == ActivityStatus.IN_PROGRESS) {
          // For ongoing activities, all are CONFIRMED
          status = ParticipationStatus.VERIFIED;
        } else {
          // For upcoming activities, mix of REGISTERED, CONFIRMED, WAITLISTED
          double rand = random.nextDouble();
          if (rand < 0.7) {
            status = ParticipationStatus.VERIFIED;
          } else if (rand < 0.9) {
            status = ParticipationStatus.UNVERIFIED;
          }
          // else {
          // status = ParticipationStatus;
          // }
        }

        // Randomly select a verifier for some participants
        EAccountCredentials verifier = null;
        if (random.nextDouble() < 0.3) { // 30% chance of having a verifier
          verifier = shuffledStudents.get((i + 5) % shuffledStudents.size()); // Just pick another student for
        }

        EParticipationDetail participation = EParticipationDetail.builder()
            .participant(student)
            .activity(activity)
            .participationStatus(status)
            .participationRole(role)
            .verifiedByAccount(verifier)
            .createdBy(student.getFullName())
            .registeredAt(Instant.parse("2025-05-01T17:00:00Z"))
            .build();

        participations.add(participation);
      }
    }

    return participations;
  }

  private void updateActivityParticipantCounts(List<EActivity> activities,
      List<EParticipationDetail> participations) {
    // Create a map to count participants per activity
    java.util.Map<Long, Integer> participantCounts = new java.util.HashMap<>();

    // Count confirmed/attended participants per activity
    for (EParticipationDetail participation : participations) {
      if (participation.getParticipationStatus() == ParticipationStatus.VERIFIED ||
          participation.getParticipationStatus() == ParticipationStatus.UNVERIFIED) {

        Long activityId = participation.getActivity().getId();
        participantCounts.put(activityId, participantCounts.getOrDefault(activityId, 0) + 1);
      }
    }

    // Update activity participant counts
    for (EActivity activity : activities) {
      activity.setCurrentParticipants(participantCounts.getOrDefault(activity.getId(), 0));
    }
  }

  private List<EFeedback> createFeedbacks(List<EParticipationDetail> participations) {
    List<EFeedback> feedbacks = new ArrayList<>();

    // Sample feedback comments
    String[] positiveComments = {
        "Really enjoyed this activity. Well organized and educational.",
        "Great experience! I learned a lot and met interesting people.",
        "The organizers did an excellent job. Would participate again.",
        "Very informative and engaging. Exceeded my expectations.",
        "Well-structured activity with valuable content. Thank you!"
    };

    String[] neutralComments = {
        "Decent activity, though some parts could be improved.",
        "Good overall, but timing could have been better.",
        "Some interesting aspects, but room for improvement.",
        "Acceptable organization, but content was somewhat basic.",
        "It was okay, but didn't fully meet my expectations."
    };

    String[] negativeComments = {
        "Poor organization made this experience disappointing.",
        "Content wasn't as advertised. Wouldn't recommend.",
        "Technical issues throughout made it difficult to follow.",
        "Presenters seemed unprepared. Expected more depth.",
        "Schedule was disorganized and activities felt rushed."
    };

    for (EParticipationDetail participation : participations) {
      // Only create feedback for ATTENDED participations (70% chance)
      if (participation.getParticipationStatus() == ParticipationStatus.VERIFIED && random.nextDouble() < 0.7) {
        double rating = 0.0;
        String comment;

        // Determine rating and comment
        double rand = random.nextDouble();
        if (rand < 0.6) { // 60% positive
          rating = 7.0 + random.nextDouble() * 3.0; // 7-10
          comment = positiveComments[random.nextInt(positiveComments.length)];
        } else if (rand < 0.9) { // 30% neutral
          rating = 4.0 + random.nextDouble() * 3.0; // 4-7
          comment = neutralComments[random.nextInt(neutralComments.length)];
        } else { // 10% negative
          rating = 1.0 + random.nextDouble() * 3.0; // 1-4
          comment = negativeComments[random.nextInt(negativeComments.length)];
        }

        EFeedback feedback = EFeedback.builder()
            .activity(participation.getActivity())
            .participation(participation)
            .rating(rating)
            .feedbackDescription(comment)
            .build();

        feedbacks.add(feedback);
      }
    }

    return feedbacks;
  }

  private List<ENotification> createNotifications(List<EAccountCredentials> accounts, List<EActivity> activities) {
    List<ENotification> notifications = new ArrayList<>();
    Map<Long, NotificationType> notificationTypeMap = Map.of(
        1L, NotificationType.SECURITY, // Admin
        2L, NotificationType.LEARNING, // Student
        3L, NotificationType.ACTIVITY // Organization
    );
    // Create various types of notifications
    for (int i = 0; i < 50; i++) { // Create 50 random notifications
      EAccountCredentials recipient = accounts.get(Integer.valueOf(i % notificationTypeMap.size() + 1));
      EActivity relatedActivity = activities.get(random.nextInt(activities.size()));

      // Sample notification messages
      String[] messages = {
          "Reminder: " + relatedActivity.getActivityName() + " is starting soon!",
          "Your registration for " + relatedActivity.getActivityName() + " has been confirmed.",
          "New activity alert: " + relatedActivity.getActivityName() + " has been posted.",
          "Your feedback for " + relatedActivity.getActivityName() + " has been received.",
          "Important update regarding " + relatedActivity.getActivityName() + ".",
          "Schedule change for " + relatedActivity.getActivityName() + ".",
          "Thank you for participating in " + relatedActivity.getActivityName() + "!"
      };

      String message = messages[random.nextInt(messages.length)];

      ENotification notification = ENotification.builder()
          .title(message)
          .notificationType(notificationTypeMap.get(Long.valueOf(i % notificationTypeMap.size() + 1)))
          .receiver(recipient)
          .content(message)
          .isRead(random.nextBoolean())
          .build();

      notifications.add(notification);
    }

    return notifications;
  }

  private List<EReport> createReports(List<EActivity> activities, List<EAccountCredentials> students) {
    List<EReport> reports = new ArrayList<>();

    // Create reports for 30% of activities
    for (EActivity activity : activities) {
      if (random.nextDouble() < 0.3) { // 30% chance of having a report
        EAccountCredentials reporter = students.get(random.nextInt(students.size()));

        String[] reportTypes = {
            "Activity Summary",
            "Participation Report",
            "Financial Summary",
            "Feedback Analysis",
            "Event Outcomes"
        };

        String[] contents = {
            "Detailed summary of the activity outcomes and achievements.",
            "Analysis of participant demographics and engagement levels.",
            "Breakdown of expenses and revenue generated from the activity.",
            "Summary of participant feedback and suggestions for improvement.",
            "Evaluation of the activity's goals and objectives achievement."
        };

        int typeIndex = random.nextInt(reportTypes.length);

        EReport report = EReport.builder()
            .activity(activity)
            .reporter(reporter)
            .reportType(reportTypes[typeIndex])
            .description(contents[typeIndex])
            .createdDate(activity.getEndDate().plusSeconds(60 * 60 * 24 * (1 + random.nextInt(5)))) // 1-5
            .build();
        reports.add(report);
      }
    }

    return reports;
  }

  // private List<EFeedback> createConfirmations(List<EActivity> activities) {
  // List<EFeedback> feedbacks = new ArrayList<>();

  // // Create confirmations for activities that need approval
  // for (EActivity activity : activities) {
  // if (activity.getStatus() == ActivityStatus.PENDING ||
  // activity.getStatus() == ActivityStatus.PUBLISHED) {

  // // ConfirmationStatus status = activity.getStatus() ==
  // ActivityStatus.PUBLISHED
  // // ? ConfirmationStatus.
  // // : ConfirmationStatus.PENDING;

  // String[] comments = {
  // "Activity reviewed and meets all requirements.",
  // "Pending further details about safety measures.",
  // "Approved with minor suggestions for improvement.",
  // "Please clarify budget allocation before final approval.",
  // "Activity aligns with university guidelines."
  // };

  // EFeedback feedback = EFeedback.builder()
  // .activity(activity)
  // .feedbackDescription(comments[random.nextInt(comments.length)])
  // .build();

  // feedbacks.add(feedback);
  // }
  // }

  // return feedbacks;
  // }
}
