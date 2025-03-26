package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EReport;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<EReport, Long> {
    List<EReport> findByActivityId(Long activityId);
}
