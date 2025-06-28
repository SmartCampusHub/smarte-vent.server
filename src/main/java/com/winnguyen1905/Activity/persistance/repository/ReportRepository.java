package com.winnguyen1905.activity.persistance.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.activity.common.constant.ReportType;
import com.winnguyen1905.activity.persistance.entity.EReport;

@Repository
public interface ReportRepository extends JpaRepository<EReport, Long> {
  List<EReport> findByReportType(ReportType reportType);
  List<EReport> findByReporter_Id(Long reporterId);
  List<EReport> findByReportedObjectId(Long reportedObjectId);
  List<EReport> findByReportTypeAndReportedObjectId(ReportType reportType, Long reportedObjectId);

  /**
   * Get report statistics by type
   */
  @Query("SELECT r.reportType, COUNT(r) FROM EReport r GROUP BY r.reportType")
  List<Object[]> getReportStatisticsByType();

  /**
   * Get reporter statistics
   */
  @Query("SELECT r.reporter.id, COUNT(r) FROM EReport r WHERE r.createdDate BETWEEN :startDate AND :endDate GROUP BY r.reporter.id")
  List<Object[]> getReporterStatistics(
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  /**
   * Get reported object statistics
   */
  @Query("SELECT r.reportedObjectId, COUNT(r) FROM EReport r WHERE r.reportType = :reportType GROUP BY r.reportedObjectId")
  List<Object[]> getReportedObjectStatistics(@Param("reportType") ReportType reportType);
}
