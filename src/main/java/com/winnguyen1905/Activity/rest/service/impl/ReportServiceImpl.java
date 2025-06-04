package com.winnguyen1905.Activity.rest.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.common.constant.ReportType;
import com.winnguyen1905.Activity.model.dto.ReportCreateDto;
import com.winnguyen1905.Activity.model.viewmodel.ReportVm;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EReport;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ReportRepository;
import com.winnguyen1905.Activity.rest.service.ReportService;

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
    return reportRepository.findByReporterId(reporterId).stream()
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
  public ReportVm updateReport(Long reportId, ReportCreateDto reportDto) {
    EReport report = reportRepository.findById(reportId)
        .orElseThrow(() -> new EntityNotFoundException("Report not found"));

    report.setReportType(reportDto.getReportType());
    report.setReportedObjectId(reportDto.getReportedObjectId());
    report.setTitle(reportDto.getTitle());
    report.setDescription(reportDto.getDescription());

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
        .build();
  }
}
