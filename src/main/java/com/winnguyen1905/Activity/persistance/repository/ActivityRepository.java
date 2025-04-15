package com.winnguyen1905.Activity.persistance.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EActivity;

@Repository
public interface ActivityRepository extends JpaRepository<EActivity, Long>, JpaSpecificationExecutor<EActivity> {
  @Query("SELECT a FROM EActivity a WHERE a.id IN :ids")
  Page<EActivity> findAllByIds(@Param("ids") List<Long> ids, Pageable pageable);

  @Query("SELECT a FROM EActivity a WHERE a.startDate BETWEEN :start AND :end")
  List<EActivity> findActivitiesStartingInRange(Instant start, Instant end);
}
