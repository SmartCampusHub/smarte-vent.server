package com.winnguyen1905.Activity.model.dto;

import java.time.LocalDateTime;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleDto {
    private Long activityId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String activityDescription;
    private ScheduleStatus status;
    private String location;
}
