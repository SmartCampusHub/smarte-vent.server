package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

public record EventScheduleVm(
    Long id,
    Long activityId,
    Instant startTime,
    Instant endTime,
    String location,
    String description,
    String status
) {} 
