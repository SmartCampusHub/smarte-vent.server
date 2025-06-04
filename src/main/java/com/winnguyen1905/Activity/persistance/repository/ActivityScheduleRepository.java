package com.winnguyen1905.Activity.persistance.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.EActivitySchedule;

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
}
