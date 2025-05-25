package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportVm  implements AbstractModel {
    private Long id;
    private Long activityId;
    private String activityName;
    private Long reportedById;
    private String reportedByName;
    private String description;
    private String createdBy;
    private Instant createdDate;
}
