package com.winnguyen1905.activity.rest.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.ReportStatus;
import com.winnguyen1905.activity.common.constant.ReportType;
import com.winnguyen1905.activity.exception.BadRequestException;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.dto.AdminUpdateReport;
import com.winnguyen1905.activity.model.dto.ReportCreateDto;
import com.winnguyen1905.activity.model.viewmodel.ReportVm;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.EReport;
import com.winnguyen1905.activity.persistance.repository.AccountRepository;
import com.winnguyen1905.activity.persistance.repository.ReportRepository;
import com.winnguyen1905.activity.rest.service.ReportService;
import com.winnguyen1905.activity.rest.service.AuthorizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for managing reports.
 * Handles report creation, retrieval, updating, and deletion with proper validation
 * and authorization checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

  private final ReportRepository reportRepository;
  private final AccountRepository accountRepository;
  private final AuthorizationService authorizationService;

  private static final String REPORT_NOT_FOUND = "Report not found with ID: %d";
  private static final String REPORTER_NOT_FOUND = "Reporter not found with ID: %d";
  private static final String INVALID_REPORT_TYPE = "Invalid report type: %s";

  /**
   * Creates a new report with validation.
   *
   * @param reportDto The report data to create
   * @param reporterId The ID of the user creating the report
   * @return The created report view model
   * @throws BadRequestException if the report data is invalid
   * @throws ResourceNotFoundException if the reporter is not found
   */
  @Override
  public ReportVm createReport(ReportCreateDto reportDto, Long reporterId) {
    log.info("Creating report for reporter ID: {}", reporterId);
    
    validateReportData(reportDto);
    
    EAccountCredentials reporter = findReporterById(reporterId);
    
    EReport report = buildReport(reportDto, reporter);
    
    EReport savedReport = reportRepository.save(report);
    
    log.info("Report created successfully with ID: {}", savedReport.getId());
    
    return mapToReportVm(savedReport);
  }

  /**
   * Retrieves a report by its ID.
   *
   * @param reportId The ID of the report to retrieve
   * @return The report view model
   * @throws ResourceNotFoundException if the report is not found
   */
  @Override
  @Transactional(readOnly = true)
  public ReportVm getReportById(Long reportId) {
    log.debug("Retrieving report with ID: {}", reportId);
    
    EReport report = findReportById(reportId);
    
    return mapToReportVm(report);
  }

  /**
   * Retrieves all reports in the system.
   *
   * @return List of all report view models
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportVm> getAllReports() {
    log.debug("Retrieving all reports");
    
    return reportRepository.findAll().stream()
        .map(this::mapToReportVm)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves reports filtered by type.
   *
   * @param reportType The report type to filter by
   * @return List of filtered report view models
   * @throws BadRequestException if the report type is invalid
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportVm> getReportsByType(String reportType) {
    log.debug("Retrieving reports by type: {}", reportType);
    
    ReportType type = parseReportType(reportType);
    
    return reportRepository.findByReportType(type).stream()
        .map(this::mapToReportVm)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves reports created by a specific reporter.
   *
   * @param reporterId The ID of the reporter
   * @return List of reports created by the reporter
   */
  @Override
  @Transactional(readOnly = true)
  public List<ReportVm> getReportsByReporter(Long reporterId) {
    log.debug("Retrieving reports by reporter ID: {}", reporterId);
    
    return reportRepository.findByReporter_Id(reporterId).stream()
        .map(this::mapToReportVm)
        .collect(Collectors.toList());
  }

  /**
   * Deletes a report by its ID.
   *
   * @param reportId The ID of the report to delete
   * @throws ResourceNotFoundException if the report is not found
   */
  @Override
  public void deleteReport(Long reportId) {
    log.info("Deleting report with ID: {}", reportId);
    
    if (!reportRepository.existsById(reportId)) {
      throw new ResourceNotFoundException(String.format(REPORT_NOT_FOUND, reportId));
    }
    
    reportRepository.deleteById(reportId);
    
    log.info("Report deleted successfully with ID: {}", reportId);
  }

  /**
   * Updates a report status and review information by admin.
   *
   * @param accountRequest The admin account making the update
   * @param adminUpdateReport The update data
   * @return The updated report view model
   * @throws ResourceNotFoundException if the report is not found
   */
  @Override
  public ReportVm updateReport(TAccountRequest accountRequest, AdminUpdateReport adminUpdateReport) {
    log.info("Updating report ID: {} by admin: {}", 
             adminUpdateReport.getReportId(), accountRequest.getId());
    
    // Authorization check: Only admins can update reports
    authorizationService.requireAdmin(accountRequest);
    
    validateAdminUpdateData(adminUpdateReport);
    
    EReport report = findReportById(adminUpdateReport.getReportId());
    
    updateReportStatus(report, adminUpdateReport, accountRequest.getId());
    
    EReport updatedReport = reportRepository.save(report);
    
    log.info("Report updated successfully with ID: {}", updatedReport.getId());
    
    return mapToReportVm(updatedReport);
  }

  /**
   * Validates report creation data.
   *
   * @param reportDto The report data to validate
   * @throws BadRequestException if the data is invalid
   */
  private void validateReportData(ReportCreateDto reportDto) {
    if (reportDto == null) {
      throw new BadRequestException("Report data cannot be null");
    }
    
    if (reportDto.getReportType() == null) {
      throw new BadRequestException("Report type is required");
    }
    
    if (reportDto.getReportedObjectId() == null) {
      throw new BadRequestException("Reported object ID is required");
    }
    
    if (!StringUtils.hasText(reportDto.getTitle())) {
      throw new BadRequestException("Report title cannot be empty");
    }
    
    if (!StringUtils.hasText(reportDto.getDescription())) {
      throw new BadRequestException("Report description cannot be empty");
    }
  }

  /**
   * Validates admin update data.
   *
   * @param adminUpdateReport The update data to validate
   * @throws BadRequestException if the data is invalid
   */
  private void validateAdminUpdateData(AdminUpdateReport adminUpdateReport) {
    if (adminUpdateReport == null) {
      throw new BadRequestException("Update data cannot be null");
    }
    
    if (adminUpdateReport.getReportId() == null) {
      throw new BadRequestException("Report ID is required");
    }
  }

  /**
   * Finds a reporter by ID.
   *
   * @param reporterId The reporter ID to find
   * @return The found reporter
   * @throws ResourceNotFoundException if the reporter is not found
   */
  private EAccountCredentials findReporterById(Long reporterId) {
    return accountRepository.findById(reporterId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(REPORTER_NOT_FOUND, reporterId)));
  }

  /**
   * Finds a report by ID.
   *
   * @param reportId The report ID to find
   * @return The found report
   * @throws ResourceNotFoundException if the report is not found
   */
  private EReport findReportById(Long reportId) {
    return reportRepository.findById(reportId)
        .orElseThrow(() -> new ResourceNotFoundException(String.format(REPORT_NOT_FOUND, reportId)));
  }

  /**
   * Parses a report type string to enum.
   *
   * @param reportType The report type string
   * @return The parsed ReportType enum
   * @throws BadRequestException if the type is invalid
   */
  private ReportType parseReportType(String reportType) {
    try {
      return ReportType.valueOf(reportType.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(String.format(INVALID_REPORT_TYPE, reportType));
    }
  }

  /**
   * Builds a report entity from creation data.
   *
   * @param reportDto The report creation data
   * @param reporter The reporter account
   * @return The built report entity
   */
  private EReport buildReport(ReportCreateDto reportDto, EAccountCredentials reporter) {
    return EReport.builder()
        .reportType(reportDto.getReportType())
        .reportedObjectId(reportDto.getReportedObjectId())
        .title(reportDto.getTitle())
        .description(reportDto.getDescription())
        .reporter(reporter)
        .status(determineInitialStatus(reportDto))
        .isReviewed(false)
        .build();
  }

  /**
   * Determines the initial status for a new report.
   *
   * @param reportDto The report creation data
   * @return The initial status
   */
  private ReportStatus determineInitialStatus(ReportCreateDto reportDto) {
    return reportDto.getStatus() != null ? reportDto.getStatus() : ReportStatus.SPENDING;
  }

  /**
   * Updates report status and review information.
   *
   * @param report The report to update
   * @param updateData The update data
   * @param reviewerId The ID of the reviewer
   */
  private void updateReportStatus(EReport report, AdminUpdateReport updateData, Long reviewerId) {
    if (updateData.getStatus() != null) {
      report.setStatus(updateData.getStatus());
      
      if (isReviewedStatus(updateData.getStatus())) {
        markAsReviewed(report, updateData, reviewerId);
      }
    }
  }

  /**
   * Checks if the status indicates the report has been reviewed.
   *
   * @param status The report status
   * @return true if the status indicates review completion
   */
  private boolean isReviewedStatus(ReportStatus status) {
    return status != ReportStatus.SPENDING;
  }

  /**
   * Marks a report as reviewed with review details.
   *
   * @param report The report to mark as reviewed
   * @param updateData The update data containing review information
   * @param reviewerId The ID of the reviewer
   */
  private void markAsReviewed(EReport report, AdminUpdateReport updateData, Long reviewerId) {
    report.setIsReviewed(true);
    report.setReviewedAt(Instant.now());
    report.setReviewerId(reviewerId);
    report.setReviewerResponse(updateData.getReviewerResponse());
  }

  /**
   * Maps a report entity to a view model.
   *
   * @param report The report entity to map
   * @return The mapped view model
   */
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
