package com.winnguyen1905.Activity.model.dto;

import java.time.Instant;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleDto {
    private Long activityId;
    private Instant startTime;
    private Instant endTime;
    private String activityDescription;
    private ScheduleStatus status;
    private String location;
}
