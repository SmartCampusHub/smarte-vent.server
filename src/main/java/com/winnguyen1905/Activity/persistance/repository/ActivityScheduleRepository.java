package com.winnguyen1905.activity.persistance.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.activity.common.constant.ScheduleStatus;
import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EActivitySchedule;

@Repository
public interface ActivityScheduleRepository extends JpaRepository<EActivitySchedule, Long> {
  
  // ==================== BASIC CRUD OPERATIONS ====================
  
  /**
   * Delete all schedules for a specific activity
   */
  @Modifying
  @Query("DELETE FROM EActivitySchedule s WHERE s.activity.id = :activityId")
  void deleteByActivityId(@Param("activityId") Long activityId);

  /**
   * Find all schedules for a specific activity
   */
  List<EActivitySchedule> findByActivity(EActivity activity);

  /**
   * Find all schedules for a specific activity ID
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId ORDER BY s.startTime ASC")
  List<EActivitySchedule> findByActivityId(@Param("activityId") Long activityId);

  /**
   * Find paginated schedules for a specific activity
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId ORDER BY s.startTime ASC")
  Page<EActivitySchedule> findByActivityId(@Param("activityId") Long activityId, Pageable pageable);

  // ==================== TIME-BASED QUERIES ====================

  /**
   * Find all activity schedules starting between the given start and end dates.
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.startTime BETWEEN :start AND :end ORDER BY s.startTime ASC")
  List<EActivitySchedule> findSchedulesStartingBetween(@Param("start") Instant start, @Param("end") Instant end);

  /**
   * Find all activity schedules ending between the given start and end dates.
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.endTime BETWEEN :start AND :end ORDER BY s.endTime ASC")
  List<EActivitySchedule> findSchedulesEndingBetween(@Param("start") Instant start, @Param("end") Instant end);

  /**
   * Find schedules that are active during a specific time period
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.startTime <= :endTime AND s.endTime >= :startTime ORDER BY s.startTime ASC")
  List<EActivitySchedule> findSchedulesActiveDuring(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

  /**
   * Find upcoming schedules (starting after current time)
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.startTime > :currentTime ORDER BY s.startTime ASC")
  List<EActivitySchedule> findUpcomingSchedules(@Param("currentTime") Instant currentTime);

  /**
   * Find current schedules (currently in progress)
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.startTime <= :currentTime AND s.endTime >= :currentTime ORDER BY s.startTime ASC")
  List<EActivitySchedule> findCurrentSchedules(@Param("currentTime") Instant currentTime);

  /**
   * Find past schedules (already ended)
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.endTime < :currentTime ORDER BY s.endTime DESC")
  List<EActivitySchedule> findPastSchedules(@Param("currentTime") Instant currentTime);

  // ==================== STATUS-BASED QUERIES ====================

  /**
   * Find schedules by status
   */
  List<EActivitySchedule> findByStatus(ScheduleStatus status);

  /**
   * Find schedules by activity and status
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId AND s.status = :status")
  List<EActivitySchedule> findByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") ScheduleStatus status);

  /**
   * Find schedules that should be marked as IN_PROGRESS
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.status = 'WAITING_TO_START' AND s.startTime <= :currentTime")
  List<EActivitySchedule> findSchedulesToStartNow(@Param("currentTime") Instant currentTime);

  /**
   * Find schedules that should be marked as COMPLETED
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.status = 'IN_PROGRESS' AND s.endTime <= :currentTime")
  List<EActivitySchedule> findSchedulesToCompleteNow(@Param("currentTime") Instant currentTime);

  // ==================== CONFLICT DETECTION ====================

  /**
   * Check for schedule conflicts - find overlapping schedules for the same activity
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId AND s.id != :excludeId AND " +
         "s.status NOT IN ('CANCELLED') AND " +
         "((s.startTime < :endTime AND s.endTime > :startTime))")
  List<EActivitySchedule> checkScheduleConflicts(
      @Param("activityId") Long activityId,
      @Param("startTime") Instant startTime,
      @Param("endTime") Instant endTime,
      @Param("excludeId") Long excludeId);

  /**
   * Check for schedule conflicts for new schedule (no excludeId)
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId AND " +
         "s.status NOT IN ('CANCELLED') AND " +
         "((s.startTime < :endTime AND s.endTime > :startTime))")
  List<EActivitySchedule> checkScheduleConflicts(
      @Param("activityId") Long activityId,
      @Param("startTime") Instant startTime,
      @Param("endTime") Instant endTime);

  /**
   * Check if a specific time slot is available for an activity
   */
  @Query("SELECT COUNT(s) FROM EActivitySchedule s WHERE s.activity.id = :activityId AND " +
         "s.status NOT IN ('CANCELLED') AND " +
         "((s.startTime < :endTime AND s.endTime > :startTime))")
  Long countConflictingSchedules(
      @Param("activityId") Long activityId,
      @Param("startTime") Instant startTime,
      @Param("endTime") Instant endTime);

  // ==================== ORGANIZATION-BASED QUERIES ====================

  /**
   * Find all schedules for activities owned by a specific organization
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.organization.id = :organizationId ORDER BY s.startTime ASC")
  List<EActivitySchedule> findByOrganizationId(@Param("organizationId") Long organizationId);

  /**
   * Find current and upcoming schedules for an organization
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.organization.id = :organizationId AND " +
         "s.endTime >= :currentTime ORDER BY s.startTime ASC")
  List<EActivitySchedule> findCurrentAndUpcomingByOrganizationId(@Param("organizationId") Long organizationId, @Param("currentTime") Instant currentTime);

  // ==================== STATISTICS QUERIES ====================

  /**
   * Get schedule statistics by status
   */
  @Query("SELECT s.status, COUNT(s) FROM EActivitySchedule s GROUP BY s.status")
  List<Object[]> getScheduleStatisticsByStatus();

  /**
   * Get schedule statistics by status for a specific organization
   */
  @Query("SELECT s.status, COUNT(s) FROM EActivitySchedule s WHERE s.activity.organization.id = :organizationId GROUP BY s.status")
  List<Object[]> getScheduleStatisticsByStatusForOrganization(@Param("organizationId") Long organizationId);

  /**
   * Get schedule count by time period
   */
  @Query("SELECT COUNT(s) FROM EActivitySchedule s WHERE s.startTime BETWEEN :startDate AND :endDate")
  Long getScheduleCountByPeriod(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  /**
   * Get schedule duration statistics
   */
  @Query("SELECT AVG(EXTRACT(EPOCH FROM (s.endTime - s.startTime))/3600), " +
         "MIN(EXTRACT(EPOCH FROM (s.endTime - s.startTime))/3600), " +
         "MAX(EXTRACT(EPOCH FROM (s.endTime - s.startTime))/3600) " +
         "FROM EActivitySchedule s WHERE s.activity.id = :activityId")
  List<Object[]> getScheduleDurationStatistics(@Param("activityId") Long activityId);

  /**
   * Count total schedules for an activity
   */
  @Query("SELECT COUNT(s) FROM EActivitySchedule s WHERE s.activity.id = :activityId")
  Long countByActivityId(@Param("activityId") Long activityId);

  /**
   * Get most busy time slots (when most schedules are running)
   */
  @Query("SELECT EXTRACT(HOUR FROM s.startTime) as hour, COUNT(s) as count " +
         "FROM EActivitySchedule s GROUP BY EXTRACT(HOUR FROM s.startTime) ORDER BY count DESC")
  List<Object[]> getMostBusyTimeSlots();

  // ==================== BULK OPERATIONS ====================

  /**
   * Update all schedules status for a specific activity
   */
  @Modifying
  @Query("UPDATE EActivitySchedule s SET s.status = :newStatus, s.updatedBy = :updatedBy WHERE s.activity.id = :activityId")
  int updateAllSchedulesStatusForActivity(
      @Param("activityId") Long activityId,
      @Param("newStatus") ScheduleStatus newStatus,
      @Param("updatedBy") String updatedBy);

  /**
   * Cancel all future schedules for an activity
   */
  @Modifying
  @Query("UPDATE EActivitySchedule s SET s.status = 'CANCELLED', s.updatedBy = :updatedBy " +
         "WHERE s.activity.id = :activityId AND s.startTime > :currentTime")
  int cancelFutureSchedulesForActivity(
      @Param("activityId") Long activityId,
      @Param("currentTime") Instant currentTime,
      @Param("updatedBy") String updatedBy);

  // ==================== SEARCH AND FILTERING ====================

  /**
   * Find schedules by location
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))")
  List<EActivitySchedule> findByLocationContaining(@Param("location") String location);

  /**
   * Find schedules by activity name pattern
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE LOWER(s.activity.activityName) LIKE LOWER(CONCAT('%', :activityName, '%'))")
  List<EActivitySchedule> findByActivityNameContaining(@Param("activityName") String activityName);

  /**
   * Complex search with multiple criteria
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE " +
         "(:activityId IS NULL OR s.activity.id = :activityId) AND " +
         "(:status IS NULL OR s.status = :status) AND " +
         "(:location IS NULL OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
         "(:startDate IS NULL OR s.startTime >= :startDate) AND " +
         "(:endDate IS NULL OR s.endTime <= :endDate) " +
         "ORDER BY s.startTime ASC")
  Page<EActivitySchedule> findSchedulesWithFilters(
      @Param("activityId") Long activityId,
      @Param("status") ScheduleStatus status,
      @Param("location") String location,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      Pageable pageable);

  // ==================== SPECIALIZED QUERIES ====================

  /**
   * Find the next schedule for an activity
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId AND s.startTime > :currentTime " +
         "ORDER BY s.startTime ASC")
  Optional<EActivitySchedule> findNextScheduleForActivity(@Param("activityId") Long activityId, @Param("currentTime") Instant currentTime);

  /**
   * Find the current active schedule for an activity
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId AND " +
         "s.startTime <= :currentTime AND s.endTime >= :currentTime")
  Optional<EActivitySchedule> findCurrentActiveScheduleForActivity(@Param("activityId") Long activityId, @Param("currentTime") Instant currentTime);

  /**
   * Get total duration of all schedules for an activity (in hours)
   */
  @Query("SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (s.endTime - s.startTime))/3600), 0) " +
         "FROM EActivitySchedule s WHERE s.activity.id = :activityId")
  Double getTotalDurationHoursForActivity(@Param("activityId") Long activityId);

  /**
   * Find schedules that need status update (based on current time)
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE " +
         "(s.status = 'WAITING_TO_START' AND s.startTime <= :currentTime) OR " +
         "(s.status = 'IN_PROGRESS' AND s.endTime <= :currentTime)")
  List<EActivitySchedule> findSchedulesNeedingStatusUpdate(@Param("currentTime") Instant currentTime);
}
