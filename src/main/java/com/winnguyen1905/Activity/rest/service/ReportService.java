package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ReportDto;
import com.winnguyen1905.Activity.model.viewmodel.ReportVm;

import java.util.List;

public interface ReportService {
    void createReport(TAccountRequest accountRequest, ReportDto reportDto);
    void deleteReport(TAccountRequest accountRequest, Long id);
    ReportVm getReportById(Long id);
    List<ReportVm> getReportsByActivityId(Long activityId);
    List<ReportVm> getReportsByReporterId(Long reporterId);
}
