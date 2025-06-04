package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.ReportType;

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
} 
