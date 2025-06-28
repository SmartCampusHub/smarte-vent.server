package com.winnguyen1905.activity.rest.service;

import java.util.List;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.AdminUpdateReport;
import com.winnguyen1905.activity.model.dto.ReportCreateDto;
import com.winnguyen1905.activity.model.viewmodel.ReportVm;

public interface ReportService {
  ReportVm createReport(ReportCreateDto reportDto, Long reporterId);

  ReportVm getReportById(Long reportId);

  List<ReportVm> getAllReports();

  List<ReportVm> getReportsByType(String reportType);

  List<ReportVm> getReportsByReporter(Long reporterId);

  void deleteReport(Long reportId);

  ReportVm updateReport(TAccountRequest accountRequest, AdminUpdateReport adminUpdateReport);
}
