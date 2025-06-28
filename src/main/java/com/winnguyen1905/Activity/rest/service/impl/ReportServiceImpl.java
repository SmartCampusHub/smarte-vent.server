package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ReportStatus;
import com.winnguyen1905.activity.common.constant.ReportType;
import com.winnguyen1905.activity.model.dto.AdminUpdateReport;
import com.winnguyen1905.activity.model.dto.ReportCreateDto;
import com.winnguyen1905.activity.model.viewmodel.ReportVm;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EReport;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ReportRepository;
import com.winnguyen1905.activity.rest.service.ReportService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReportServiceImpl implements ReportService {

  @Autowired
  private ReportRepository reportRepository;

  @Autowired
  private AccountRepository accountCredentialsRepository;

  @Override
  public ReportVm createReport(ReportCreateDto reportDto, Long reporterId) {
    EAccountCredentials reporter = accountCredentialsRepository.findById(reporterId)
        .orElseThrow(() -> new EntityNotFoundException("Reporter not found"));

    EReport report = EReport.builder()
        .reportType(reportDto.getReportType())
        .reportedObjectId(reportDto.getReportedObjectId())
        .title(reportDto.getTitle())
        .description(reportDto.getDescription())
        .reporter(reporter)
        .status(reportDto.getStatus() != null ? reportDto.getStatus() : ReportStatus.SPENDING)
        .isReviewed(false)
        .build();

    EReport savedReport = reportRepository.save(report);
    return mapToReportVm(savedReport);
  }

  @Override
  public ReportVm getReportById(Long reportId) {
    EReport report = reportRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("Report not found"));
    return mapToReportVm(report);
  }

  @Override
  public List<ReportVm> getAllReports() {
    return reportRepository.findAll().stream()
        .map(this::mapToReportVm)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportVm> getReportsByType(String reportType) {
    ReportType type = ReportType.valueOf(reportType.toUpperCase());
    return reportRepository.findByReportType(type).stream()
        .map(this::mapToReportVm)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportVm> getReportsByReporter(Long reporterId) {
    return reportRepository.findByReporter_Id(reporterId).stream()
        .map(this::mapToReportVm)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteReport(Long reportId) {
    if (!reportRepository.existsById(reportId)) {
      throw new EntityNotFoundException("Report not found");
    }
    reportRepository.deleteById(reportId);
  }

  @Override
  public ReportVm updateReport(TAccountRequest accountRequest, AdminUpdateReport adminUpdateReport) {
    EReport report = reportRepository.findById(adminUpdateReport.getReportId())
        .orElseThrow(() -> new EntityNotFoundException("Report not found"));

    if (adminUpdateReport.getStatus() != null) {
      report.setStatus(adminUpdateReport.getStatus());
      if (adminUpdateReport.getStatus() != ReportStatus.SPENDING) {
        report.setIsReviewed(true);
        report.setReviewedAt(Instant.now());
        report.setReviewerId(1L);
        report.setReviewerResponse(adminUpdateReport.getReviewerResponse());
      }
    }

    EReport updatedReport = reportRepository.save(report);
    return mapToReportVm(updatedReport);
  }

  private ReportVm mapToReportVm(EReport report) {
    return ReportVm.builder()
        .id(report.getId())
        .reportType(report.getReportType())
        .reportedObjectId(report.getReportedObjectId())
        .title(report.getTitle())
        .description(report.getDescription())
        .reporterId(report.getReporter().getId())
        .reporterName(report.getReporter().getFullName())
        .createdDate(report.getCreatedDate())
        .status(report.getStatus())
        .isReviewed(report.getIsReviewed())
        .reviewedAt(report.getReviewedAt())
        .reviewerId(report.getReviewerId())
        .reviewerResponse(report.getReviewerResponse())
        .build();
  }
}
