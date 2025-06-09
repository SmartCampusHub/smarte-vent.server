package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.winnguyen1905.Activity.common.constant.ReportStatus;
import com.winnguyen1905.Activity.common.constant.ReportType;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportVm implements AbstractModel {
  private Long id;
  private ReportType reportType;
  private Long reportedObjectId;
  private String title;
  private String description;
  private Long reporterId;
  private String reporterName;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant createdDate;
  
  private ReportStatus status;
  private Boolean isReviewed;
  
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant reviewedAt;
  private Long reviewerId;
  private String reviewerResponse;
}
