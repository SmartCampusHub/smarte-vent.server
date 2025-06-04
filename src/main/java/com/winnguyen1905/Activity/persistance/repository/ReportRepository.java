package com.winnguyen1905.Activity.persistance.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.common.constant.ReportType;
import com.winnguyen1905.Activity.persistance.entity.EReport;

@Repository
public interface ReportRepository extends JpaRepository<EReport, Long> {
  List<EReport> findByReportType(ReportType reportType);
  List<EReport> findByReporterId(Long reporterId);
  List<EReport> findByReportedObjectId(Long reportedObjectId);
  List<EReport> findByReportTypeAndReportedObjectId(ReportType reportType, Long reportedObjectId);

  /**
   * Get report statistics by type
   */
  @Query(value = "CALL get_report_statistics_by_type()", nativeQuery = true)
  List<Map<String, Object>> getReportStatisticsByType();

  /**
   * Get reporter statistics
   */
  @Query(value = "CALL get_reporter_statistics(:startDate, :endDate)", nativeQuery = true)
  List<Map<String, Object>> getReporterStatistics(
      @Param("startDate") String startDate,
      @Param("endDate") String endDate
  );

  /**
   * Get reported object statistics
   */
  @Query(value = "CALL get_reported_object_statistics(:reportType)", nativeQuery = true)
  List<Map<String, Object>> getReportedObjectStatistics(
      @Param("reportType") String reportType
  );
}
