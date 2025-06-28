package com.winnguyen1905.activity.persistance.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.activity.persistance.entity.EActivity;
import com.winnguyen1905.activity.persistance.entity.EActivitySchedule;

@Repository
public interface ActivityScheduleRepository extends JpaRepository<EActivitySchedule, Long> {
  void deleteByActivityId(Long activityId);

  /**
   * Find all activity schedules starting between the given start and end dates.
   * 
   * @param start Start date
   * @param end   End date
   * @return List of activity schedules
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.startTime BETWEEN :start AND :end")
  List<EActivitySchedule> findSchedulesStartingBetween(@Param("start") Instant start, @Param("end") Instant end);

  List<EActivitySchedule> findByActivity(EActivity activity);

  /**
   * Get schedule statistics by status
   */
  @Query("SELECT s.status, COUNT(s) FROM EActivitySchedule s GROUP BY s.status")
  List<Object[]> getScheduleStatisticsByStatus();

  /**
   * Check for schedule conflicts - find overlapping schedules
   */
  @Query("SELECT s FROM EActivitySchedule s WHERE s.activity.id = :activityId AND " +
         "((s.startTime <= :endTime AND s.endTime >= :startTime))")
  List<EActivitySchedule> checkScheduleConflicts(
      @Param("activityId") Long activityId,
      @Param("startTime") Instant startTime,
      @Param("endTime") Instant endTime);

  /**
   * Get schedule statistics by time period
   */
  @Query("SELECT COUNT(s) FROM EActivitySchedule s WHERE s.startTime BETWEEN :startDate AND :endDate")
  Long getScheduleCountByPeriod(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);
}
