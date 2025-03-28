package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ReportDto;
import com.winnguyen1905.Activity.model.viewmodel.ReportVm;
import com.winnguyen1905.Activity.persistance.repository.ReportRepository;
import com.winnguyen1905.Activity.rest.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;

    @Override
    public void createReport(TAccountRequest accountRequest, ReportDto reportDto) {
        // TODO: Implement create report logic
    }

    @Override
    public void deleteReport(TAccountRequest accountRequest, Long id) {
        // TODO: Implement delete report logic
    }

    @Override
    public ReportVm getReportById(Long id) {
        // TODO: Implement get report by id logic
        return null;
    }

    @Override
    public List<ReportVm> getReportsByActivityId(Long activityId) {
        // TODO: Implement get reports by activity id logic
        return null;
    }

    @Override
    public List<ReportVm> getReportsByReporterId(Long reporterId) {
        // TODO: Implement get reports by reporter id logic
        return null;
    }
}
