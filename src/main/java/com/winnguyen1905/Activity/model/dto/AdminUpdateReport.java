package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.ReportStatus;
import com.winnguyen1905.Activity.common.constant.ReportType;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateReport implements AbstractModel {
    private Long reportId;
    private ReportStatus status;
    private String reviewerResponse;
} 