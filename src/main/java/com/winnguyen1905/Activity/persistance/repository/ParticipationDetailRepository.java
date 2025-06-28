package com.winnguyen1905.activity.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.persistance.entity.EParticipationDetail;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationDetailRepository
    extends JpaRepository<EParticipationDetail, Long>, JpaSpecificationExecutor<EParticipationDetail> {
  List<EParticipationDetail> findByActivityId(Long activityId);

  @Query("SELECT e FROM EParticipationDetail e " +
      "WHERE e.participant.id = :studentId AND e.activity.id = :activityId")
  Optional<EParticipationDetail> findByStudentIdAndActivityId(@Param("studentId") Long studentId,
      @Param("activityId") Long activityId);

  Boolean existsByParticipantIdAndActivityId(Long participantId, Long activityId);

  Boolean existsByParticipantIdAndActivityIdAndParticipationRole(Long participantId, Long activityId,
      ParticipationRole participationRole);

  List<EParticipationDetail> findAllByParticipantId(Long participantId);

  @Query("SELECT e FROM EParticipationDetail e " +
      "WHERE e.registeredAt BETWEEN :startDate AND :endDate " +
      "AND e.participationStatus = :status AND e.participant.id = :participantId " +
      "ORDER BY e.registeredAt ASC ")
  List<EParticipationDetail> findVerifiedSpecificParticipationDetailsWithinDateRange(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("status") ParticipationStatus status,
      @Param("participantId") Long participantId);

  List<EParticipationDetail> findAllByParticipantIdAndParticipationRole(
      Long participantId, ParticipationRole participationRole);

  // Methods for student statistics

  // Count total activities a student has participated in
  @Query("SELECT COUNT(p) FROM EParticipationDetail p WHERE p.participant.id = :studentId")
  Long countTotalActivitiesByStudentId(@Param("studentId") Long studentId);

  // Count activities with specific role
  @Query("SELECT COUNT(p) FROM EParticipationDetail p WHERE p.participant.id = :studentId AND p.participationRole = :role")
  Long countActivitiesByStudentIdAndRole(@Param("studentId") Long studentId, @Param("role") ParticipationRole role);

  // Get average assessment score from feedbacks
  @Query("SELECT AVG(f.rating) FROM EFeedback f JOIN f.participation p WHERE p.participant.id = :studentId")
  Double getAverageAssessmentScoreByStudentId(@Param("studentId") Long studentId);

  // Sum of attendance score units across all activities
  @Query("SELECT SUM(a.attendanceScoreUnit) FROM EActivity a JOIN a.participationDetails p WHERE p.participant.id = :studentId")
  Double getTotalTrainingScoreByStudentId(@Param("studentId") Long studentId);

  // Get activities breakdown by category
  @Query("SELECT a.activityCategory, COUNT(p) FROM EParticipationDetail p JOIN p.activity a WHERE p.participant.id = :studentId GROUP BY a.activityCategory")
  List<Object[]> getActivitiesByCategory(@Param("studentId") Long studentId);

  // Get recent activities with limit
  @Query("SELECT p FROM EParticipationDetail p WHERE p.participant.id = :studentId ORDER BY p.registeredAt DESC")
  List<EParticipationDetail> getRecentActivitiesByStudentId(@Param("studentId") Long studentId, Pageable pageable);

  // Get monthly participation trend
  @Query("SELECT FUNCTION('YEAR', p.registeredAt), FUNCTION('MONTH', p.registeredAt), COUNT(p) " +
      "FROM EParticipationDetail p " +
      "WHERE p.participant.id = :studentId " +
      "AND p.registeredAt BETWEEN :startDate AND :endDate " +
      "GROUP BY FUNCTION('YEAR', p.registeredAt), FUNCTION('MONTH', p.registeredAt) " +
      "ORDER BY FUNCTION('YEAR', p.registeredAt), FUNCTION('MONTH', p.registeredAt)")
  List<Object[]> getMonthlyParticipationTrend(
      @Param("studentId") Long studentId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  // Calculate total participation hours (estimate based on activity duration)
  @Query("SELECT SUM(FUNCTION('TIMESTAMPDIFF', HOUR, a.startDate, a.endDate)) " +
      "FROM EParticipationDetail p JOIN p.activity a " +
      "WHERE p.participant.id = :studentId AND a.startDate IS NOT NULL AND a.endDate IS NOT NULL")
  Double calculateTotalParticipationHours(@Param("studentId") Long studentId);

  // Get participation count by month for the current year
  @Query("SELECT FUNCTION('MONTH', p.registeredAt), COUNT(p) " +
      "FROM EParticipationDetail p " +
      "WHERE p.participant.id = :studentId " +
      "AND FUNCTION('YEAR', p.registeredAt) = FUNCTION('YEAR', CURRENT_DATE) " +
      "GROUP BY FUNCTION('MONTH', p.registeredAt) " +
      "ORDER BY FUNCTION('MONTH', p.registeredAt)")
  List<Object[]> getCurrentYearMonthlyParticipation(@Param("studentId") Long studentId);

  // New method for feedback eligibility check
  @Query("SELECT p FROM EParticipationDetail p WHERE p.participant.id = :studentId AND p.activity.id = :activityId")
  Optional<EParticipationDetail> findByStudentIdAndActivityIdForFeedback(
      @Param("studentId") Long studentId, 
      @Param("activityId") Long activityId);
}
