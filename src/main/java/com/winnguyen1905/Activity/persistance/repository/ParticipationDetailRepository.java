package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;

import java.time.Instant;
import java.util.List;

@Repository
public interface ParticipationDetailRepository
    extends JpaRepository<EParticipationDetail, Long>, JpaSpecificationExecutor<EParticipationDetail> {
  List<EParticipationDetail> findByActivityId(Long activityId);

  Boolean existsByParticipantIdAndActivityId(Long participantId, Long activityId);

  List<EParticipationDetail> findAllByParticipantId(Long participantId);

  @Query("SELECT e FROM EParticipationDetail e " +
      "WHERE e.registeredAt BETWEEN :startDate AND :endDate " +
      "AND e.participationStatus = :status " +
      "ORDER BY e.registeredAt ASC ")
  List<EParticipationDetail> findVerifiedParticipationDetailsWithinDateRange(
      @Param("startDate") Instant startDate, 
      @Param("endDate") Instant endDate,
      @Param("status") ParticipationStatus status);

  // @Query("SELECT a FROM EParticipationDetail a WHERE a.id IN :ids")
  // Page<EParticipationDetail> findAllByIds(Long participantIds, Pageable
  // pageable);
}
