package com.winnguyen1905.Activity.rest.service;

import java.util.List;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.AdminUpdateReport;
import com.winnguyen1905.Activity.model.dto.ReportCreateDto;
import com.winnguyen1905.Activity.model.viewmodel.ReportVm;

public interface ReportService {
  ReportVm createReport(ReportCreateDto reportDto, Long reporterId);

  ReportVm getReportById(Long reportId);

  List<ReportVm> getAllReports();

  List<ReportVm> getReportsByType(String reportType);

  List<ReportVm> getReportsByReporter(Long reporterId);

  void deleteReport(Long reportId);

  ReportVm updateReport(TAccountRequest accountRequest, AdminUpdateReport adminUpdateReport);
}
