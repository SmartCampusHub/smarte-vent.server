package com.winnguyen1905.Activity.persistance.repository.TriggerRepo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;

public interface AccountRepository
      {
  Optional<EAccountCredentials> findByIdentifyCode(String identifyCode);

  Optional<EAccountCredentials> findByRefreshToken(String refreshToken);

  @Query(value = "CALL get_account_statistics_by_role()", nativeQuery = true)
  List<Map<String, Object>> getAccountStatisticsByRole();

  @Query(value = "CALL get_student_statistics_by_major()", nativeQuery = true)
  List<Map<String, Object>> getStudentStatisticsByMajor();

  @Query(value = "CALL get_account_participation_stats(:startDate, :endDate)", nativeQuery = true)
  List<Map<String, Object>> getAccountParticipationStats(
      @Param("startDate") String startDate,
      @Param("endDate") String endDate);

  @Query(value = "CALL get_account_activity_metrics(:accountId)", nativeQuery = true)
  Map<String, Object> getAccountActivityMetrics(@Param("accountId") Long accountId);
}
