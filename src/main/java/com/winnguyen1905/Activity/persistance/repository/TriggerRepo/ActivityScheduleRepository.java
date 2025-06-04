package com.winnguyen1905.Activity.persistance.repository.TriggerRepo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EActivitySchedule;

public interface ActivityScheduleRepository   {
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
  @Query(value = "CALL get_schedule_statistics_by_status()", nativeQuery = true)
  List<Map<String, Object>> getScheduleStatisticsByStatus();

  /**
   * Check for schedule conflicts
   */
  @Query(value = "CALL check_schedule_conflicts(:activityId, :startTime, :endTime)", nativeQuery = true)
  List<Map<String, Object>> checkScheduleConflicts(
      @Param("activityId") Long activityId,
      @Param("startTime") String startTime,
      @Param("endTime") String endTime
  );

  /**
   * Get schedule statistics by time period
   */
  @Query(value = "CALL get_schedule_statistics_by_period(:period, :startDate, :endDate)", nativeQuery = true)
  List<Map<String, Object>> getScheduleStatisticsByPeriod(
      @Param("period") String period,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate
  );
}
