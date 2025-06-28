package com.winnguyen1905.activity.model.dto;

import com.winnguyen1905.activity.common.constant.ReportStatus;
import com.winnguyen1905.activity.common.constant.ReportType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreateDto implements AbstractModel {
    private ReportType reportType;
    private Long reportedObjectId;
    private String title;
    private String description;
    private ReportStatus status;
    private String reviewerResponse;
} 
