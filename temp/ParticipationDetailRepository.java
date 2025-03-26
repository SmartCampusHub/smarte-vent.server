package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;

import java.util.List;

@Repository
public interface ParticipationDetailRepository extends JpaRepository<EParticipationDetail, Long> {
    List<EParticipationDetail> findByActivityId(Long activityId);
}
