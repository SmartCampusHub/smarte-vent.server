package com.winnguyen1905.Activity.persistance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.common.constant.ReportType;
import com.winnguyen1905.Activity.persistance.entity.EReport;

@Repository
public interface ReportRepository extends JpaRepository<EReport, Long> {
  List<EReport> findByReportType(ReportType reportType);
  List<EReport> findByReporterId(Long reporterId);
  List<EReport> findByReportedObjectId(Long reportedObjectId);
  List<EReport> findByReportTypeAndReportedObjectId(ReportType reportType, Long reportedObjectId);
}
