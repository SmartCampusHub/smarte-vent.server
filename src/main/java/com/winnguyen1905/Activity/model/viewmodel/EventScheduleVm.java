package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

public record EventScheduleVm(
    Long id,
    Long activityId,
    Instant startTime,
    Instant endTime,
    String location,
    String description,
    String status
)  implements AbstractModel {} 
