package com.winnguyen1905.Activity.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.AdminUpdateReport;
import com.winnguyen1905.Activity.model.dto.ReportCreateDto;
import com.winnguyen1905.Activity.model.viewmodel.ReportVm;
import com.winnguyen1905.Activity.rest.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

  @Autowired
  private ReportService reportService;

  @PostMapping
  public ResponseEntity<ReportVm> createReport(
      @RequestBody ReportCreateDto reportDto,
      @AccountRequest TAccountRequest accountRequest) {
    ReportVm createdReport = reportService.createReport(reportDto, accountRequest.id());
    return ResponseEntity.ok(createdReport);
  }

  @GetMapping("/{reportId}")
  public ResponseEntity<ReportVm> getReportById(@PathVariable Long reportId) {
    ReportVm report = reportService.getReportById(reportId);
    return ResponseEntity.ok(report);
  }

  @GetMapping
  public ResponseEntity<List<ReportVm>> getAllReports() {
    List<ReportVm> reports = reportService.getAllReports();
    return ResponseEntity.ok(reports);
  }

  @GetMapping("/type/{reportType}")
  public ResponseEntity<List<ReportVm>> getReportsByType(@PathVariable String reportType) {
    List<ReportVm> reports = reportService.getReportsByType(reportType);
    return ResponseEntity.ok(reports);
  }

  @GetMapping("/reporter/{reporterId}")
  public ResponseEntity<List<ReportVm>> getReportsByReporter(@PathVariable Long reporterId) {
    List<ReportVm> reports = reportService.getReportsByReporter(reporterId);
    return ResponseEntity.ok(reports);
  }

  @DeleteMapping("/{reportId}")
  public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
    reportService.deleteReport(reportId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/response")
  public ResponseEntity<ReportVm> updateReport(
      @AccountRequest TAccountRequest accountRequest, 
      @RequestBody AdminUpdateReport adminUpdateReport) {
    ReportVm updatedReport = reportService.updateReport(accountRequest, adminUpdateReport);
    return ResponseEntity.ok(updatedReport);
  }
}
