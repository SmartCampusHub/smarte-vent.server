package com.winnguyen1905.Activity.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportDto  implements AbstractModel {
    private Long activityId;
    private String description;
}
