package com.winnguyen1905.activity.utils;

import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EActivitySchedule;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for generating email templates for activity notifications.
 */
public class EmailTemplateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Formats an Instant to a human-readable date/time string.
     */
    private static String formatInstant(java.time.Instant instant) {
        if (instant == null) {
            return "Not specified";
        }
        return DATE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    /**
     * Generates an activity reminder email body.
     */
    public static String generateActivityReminderBody(EActivity activity, EAccountCredentials participant) {
        return String.format(
                "Dear %s,\n\n" +
                "This is a reminder that the activity '%s' is scheduled to start soon.\n\n" +
                "Date: %s\n" +
                "Location: %s\n" +
                "Description: %s\n\n" +
                "We look forward to your participation!\n\n" +
                "Best regards,\n" +
                "The Activity Management Team",
                participant.getFullName(),
                activity.getActivityName(),
                formatInstant(activity.getStartDate()),
                activity.getVenue(),
                activity.getShortDescription()
        );
    }

    /**
     * Generates a schedule reminder email body.
     */
    public static String generateScheduleReminderBody(EActivitySchedule schedule, EAccountCredentials participant) {
        return String.format(
                "Dear %s,\n\n" +
                "This is a reminder for an upcoming schedule in the activity '%s'.\n\n" +
                "Schedule: %s\n" +
                "Start Time: %s\n" +
                "End Time: %s\n" +
                "Location: %s\n" +
                "Description: %s\n\n" +
                "We look forward to your participation!\n\n" +
                "Best regards,\n" +
                "The Activity Management Team",
                participant.getFullName(),
                schedule.getActivity().getActivityName(),
                schedule.getActivityDescription(),
                formatInstant(schedule.getStartTime()),
                formatInstant(schedule.getEndTime()),
                schedule.getLocation(),
                schedule.getActivityDescription()
        );
    }

    /**
     * Generates an email for activity status change.
     */
    public static String generateActivityStatusChangeBody(EActivity activity, EAccountCredentials participant, String oldStatus, String newStatus) {
        return String.format(
                "Dear %s,\n\n" +
                "The status of activity '%s' has been updated from '%s' to '%s'.\n\n" +
                "Date: %s\n" +
                "Location: %s\n\n" +
                "If you have any questions, please contact the organizers.\n\n" +
                "Best regards,\n" +
                "The Activity Management Team",
                participant.getFullName(),
                activity.getActivityName(),
                oldStatus,
                newStatus,
                formatInstant(activity.getStartDate()),
                activity.getVenue()
        );
    }

    /**
     * Generates an email for activity registration deadline approaching.
     */
    public static String generateRegistrationDeadlineBody(EActivity activity, EAccountCredentials participant) {
        return String.format(
                "Dear %s,\n\n" +
                "The registration deadline for activity '%s' is approaching.\n\n" +
                "Registration Deadline: %s\n" +
                "Activity Date: %s\n" +
                "Location: %s\n\n" +
                "If you're interested in participating, please register soon!\n\n" +
                "Best regards,\n" +
                "The Activity Management Team",
                participant.getFullName(),
                activity.getActivityName(),
                formatInstant(activity.getRegistrationDeadline()),
                formatInstant(activity.getStartDate()),
                activity.getVenue()
        );
    }
}
