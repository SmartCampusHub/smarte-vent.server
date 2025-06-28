package com.winnguyen1905.activity.rest.service;

/**
 * Service interface for scheduling activity-related tasks.
 */
public interface ActivitySchedulingService {
    /**
     * Sends notifications and emails for upcoming activities.
     */
    void sendUpcomingActivityNotifications();
    
    /**
     * Sends notifications and emails for upcoming activity schedules.
     */
    void sendUpcomingScheduleNotifications();
    
    /**
     * Updates activity statuses based on current date and deadlines.
     */
    void updateActivityStatuses();
    
    /**
     * Sends reminders about approaching registration deadlines.
     */
    void sendRegistrationDeadlineReminders();
}
