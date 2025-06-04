package com.winnguyen1905.Activity.persistance.repository.TriggerRepo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.persistance.entity.EActivity;

public interface ActivityRepository   {
  // Find activities by category but exclude a specific activity
  List<EActivity> findByActivityCategoryAndIdNot(ActivityCategory category, Long activityId);

  @Query("SELECT a FROM EActivity a WHERE a.id IN :ids")
  Page<EActivity> findAllByIds(@Param("ids") List<Long> ids, Pageable pageable);

  @Query("SELECT a FROM EActivity a WHERE a.startDate BETWEEN :start AND :end")
  List<EActivity> findActivitiesStartingInRange(@Param("start") Instant start, @Param("end") Instant end);

  // Find activities that have ended but still marked with a specific status
  List<EActivity> findByStatusAndEndDateBefore(ActivityStatus status, Instant date);

  // Find activities with a specific status whose start date is before a given
  // date
  List<EActivity> findByStatusAndStartDateBefore(ActivityStatus status, Instant date);

  // Find activities with a specific status whose registration deadline is before
  // a given date
  List<EActivity> findByStatusAndRegistrationDeadlineBefore(ActivityStatus status, Instant date);

  // Find activities with registration deadlines between two dates
  @Query("SELECT a FROM EActivity a WHERE a.registrationDeadline BETWEEN :start AND :end")
  List<EActivity> findByRegistrationDeadlineBetween(@Param("start") Instant start, @Param("end") Instant end);

  @Query("SELECT COUNT(a) FROM EActivity a")
  Long countTotalActivities();

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a")
  Long countTotalParticipants();

  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate")
  Long countActivitiesInDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query("SELECT a.activityCategory as category, COUNT(a) as count FROM EActivity a GROUP BY a.activityCategory")
  List<Object[]> countActivitiesByCategory();

  // New filtering methods
  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category")
  Long countActivitiesByTimeAndCategory(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("category") ActivityCategory category);

  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.status = :status")
  Long countActivitiesByTimeAndStatus(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("status") ActivityStatus status);

  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.activityCategory = :category")
  Long countActivitiesByCategory(@Param("category") ActivityCategory category);

  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.status = :status")
  Long countActivitiesByStatus(@Param("status") ActivityStatus status);

  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.activityCategory = :category AND a.status = :status")
  Long countActivitiesByCategoryAndStatus(@Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category AND a.status = :status")
  Long countActivitiesByTimeAndCategoryAndStatus(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate, @Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  @Query("SELECT a.activityCategory as category, COUNT(a) as count FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate GROUP BY a.activityCategory")
  List<Object[]> countActivitiesByCategoryInTimeRange(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("SELECT a.status as status, COUNT(a) as count FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate GROUP BY a.status")
  List<Object[]> countActivitiesByStatusInTimeRange(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate")
  Long countParticipantsInTimeRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.activityCategory = :category")
  Long countParticipantsByCategory(@Param("category") ActivityCategory category);

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.status = :status")
  Long countParticipantsByStatus(@Param("status") ActivityStatus status);

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category")
  Long countParticipantsByTimeAndCategory(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("category") ActivityCategory category);

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.status = :status")
  Long countParticipantsByTimeAndStatus(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
      @Param("status") ActivityStatus status);

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.activityCategory = :category AND a.status = :status")
  Long countParticipantsByCategoryAndStatus(@Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.startDate BETWEEN :startDate AND :endDate AND a.activityCategory = :category AND a.status = :status")
  Long countParticipantsByTimeAndCategoryAndStatus(@Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate, @Param("category") ActivityCategory category,
      @Param("status") ActivityStatus status);

  // Organization-specific queries

  // Count total activities for an organization
  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.organization.id = :organizationId")
  Long countTotalActivitiesByOrganization(@Param("organizationId") Long organizationId);

  // Count activities by status for an organization
  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.organization.id = :organizationId AND a.status = :status")
  Long countActivitiesByStatusAndOrganization(
      @Param("organizationId") Long organizationId,
      @Param("status") ActivityStatus status);

  // Count upcoming activities for an organization
  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.organization.id = :organizationId AND a.startDate > CURRENT_TIMESTAMP")
  Long countUpcomingActivitiesByOrganization(@Param("organizationId") Long organizationId);

  // Count ongoing activities for an organization
  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.organization.id = :organizationId AND a.startDate <= CURRENT_TIMESTAMP AND a.endDate >= CURRENT_TIMESTAMP")
  Long countOngoingActivitiesByOrganization(@Param("organizationId") Long organizationId);

  // Count completed activities for an organization
  @Query("SELECT COUNT(a) FROM EActivity a WHERE a.organization.id = :organizationId AND a.endDate < CURRENT_TIMESTAMP")
  Long countCompletedActivitiesByOrganization(@Param("organizationId") Long organizationId);

  // Count total participants for an organization
  @Query("SELECT SUM(a.currentParticipants) FROM EActivity a WHERE a.organization.id = :organizationId")
  Long countTotalParticipantsByOrganization(@Param("organizationId") Long organizationId);

  // Calculate average participants per activity for an organization
  @Query("SELECT AVG(a.currentParticipants) FROM EActivity a WHERE a.organization.id = :organizationId")
  Double calculateAverageParticipantsPerActivityByOrganization(@Param("organizationId") Long organizationId);

  // Calculate participation rate (% of available slots filled) for an
  // organization
  @Query("SELECT SUM(a.currentParticipants) * 100.0 / SUM(a.capacityLimit) FROM EActivity a WHERE a.organization.id = :organizationId AND a.capacityLimit > 0")
  Double calculateParticipationRateByOrganization(@Param("organizationId") Long organizationId);

  // Get activities by category for an organization
  @Query("SELECT a.activityCategory, COUNT(a) FROM EActivity a WHERE a.organization.id = :organizationId GROUP BY a.activityCategory")
  List<Object[]> getActivitiesByCategoryForOrganization(@Param("organizationId") Long organizationId);

  // Get participants by category for an organization
  @Query("SELECT a.activityCategory, SUM(a.currentParticipants) FROM EActivity a WHERE a.organization.id = :organizationId GROUP BY a.activityCategory")
  List<Object[]> getParticipantsByCategoryForOrganization(@Param("organizationId") Long organizationId);

  // Get activities by month for an organization
  @Query("SELECT FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate), COUNT(a) " +
      "FROM EActivity a " +
      "WHERE a.organization.id = :organizationId " +
      "AND a.startDate BETWEEN :startDate AND :endDate " +
      "GROUP BY FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate) " +
      "ORDER BY FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate)")
  List<Object[]> getActivitiesByMonthForOrganization(
      @Param("organizationId") Long organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // Get participants by month for an organization
  @Query("SELECT FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate), SUM(a.currentParticipants) " +
      "FROM EActivity a " +
      "WHERE a.organization.id = :organizationId " +
      "AND a.startDate BETWEEN :startDate AND :endDate " +
      "GROUP BY FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate) " +
      "ORDER BY FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate)")
  List<Object[]> getParticipantsByMonthForOrganization(
      @Param("organizationId") Long organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // Get top activities by participation for an organization
  @Query("SELECT a FROM EActivity a WHERE a.organization.id = :organizationId ORDER BY a.currentParticipants DESC")
  List<EActivity> getTopActivitiesByParticipationForOrganization(
      @Param("organizationId") Long organizationId,
      Pageable pageable);

  List<EActivity> findByStatusAndIsApprovedAndStartDateLessThanEqual(
      ActivityStatus status,
      boolean isApproved,
      Instant date);

  // Stored Procedure calls
  @Procedure(name = "update_activity_status")
  void updateActivityStatus();

  @Procedure(name = "calculate_activity_statistics")
  List<Map<String, Object>> calculateActivityStatistics(
      @Param("p_start_date") Instant startDate,
      @Param("p_end_date") Instant endDate);

  @Procedure(name = "get_organization_metrics")
  Map<String, Object> getOrganizationMetrics(
      @Param("p_organization_id") Long organizationId,
      @Param("p_start_date") Instant startDate,
      @Param("p_end_date") Instant endDate);

  @Procedure(name = "get_activity_trends")
  List<Map<String, Object>> getActivityTrends(
      @Param("p_start_date") Instant startDate,
      @Param("p_end_date") Instant endDate);

  // Additional query methods for statistics
  @Query("SELECT a.activityCategory as category, " +
      "COUNT(a) as totalActivities, " +
      "SUM(a.currentParticipants) as totalParticipants, " +
      "AVG(a.currentParticipants) as avgParticipants, " +
      "SUM(a.capacityLimit) as totalCapacity, " +
      "(SUM(a.currentParticipants) * 100.0 / SUM(a.capacityLimit)) as participationRate " +
      "FROM EActivity a " +
      "WHERE a.startDate BETWEEN :startDate AND :endDate " +
      "GROUP BY a.activityCategory")
  List<Map<String, Object>> getActivityStatisticsByCategory(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("SELECT " +
      "COUNT(a) as totalActivities, " +
      "SUM(a.currentParticipants) as totalParticipants, " +
      "AVG(a.currentParticipants) as avgParticipantsPerActivity, " +
      "SUM(a.capacityLimit) as totalCapacity, " +
      "(SUM(a.currentParticipants) * 100.0 / SUM(a.capacityLimit)) as overallParticipationRate, " +
      "COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completedActivities, " +
      "COUNT(CASE WHEN a.status = 'IN_PROGRESS' THEN 1 END) as ongoingActivities, " +
      "COUNT(CASE WHEN a.status = 'PUBLISHED' THEN 1 END) as upcomingActivities " +
      "FROM EActivity a " +
      "WHERE a.organization.id = :organizationId " +
      "AND a.startDate BETWEEN :startDate AND :endDate")
  Map<String, Object> getOrganizationPerformanceMetrics(
      @Param("organizationId") Long organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query("SELECT " +
      "FUNCTION('YEAR', a.startDate) as year, " +
      "FUNCTION('MONTH', a.startDate) as month, " +
      "a.activityCategory as category, " +
      "COUNT(a) as activityCount, " +
      "SUM(a.currentParticipants) as totalParticipants, " +
      "AVG(a.currentParticipants) as avgParticipants " +
      "FROM EActivity a " +
      "WHERE a.startDate BETWEEN :startDate AND :endDate " +
      "GROUP BY FUNCTION('YEAR', a.startDate), FUNCTION('MONTH', a.startDate), a.activityCategory " +
      "ORDER BY year, month, category")
  List<Map<String, Object>> getActivityTrendsByMonth(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // Method to update activity status based on dates
  @Modifying
  @Query("UPDATE EActivity a SET " +
      "a.status = CASE " +
      "WHEN a.startDate <= CURRENT_TIMESTAMP AND a.endDate > CURRENT_TIMESTAMP THEN 'IN_PROGRESS' " +
      "WHEN a.endDate < CURRENT_TIMESTAMP THEN 'COMPLETED' " +
      "ELSE a.status END " +
      "WHERE a.status IN ('PUBLISHED', 'IN_PROGRESS')")
  void updateActivityStatusBasedOnDates();
}
