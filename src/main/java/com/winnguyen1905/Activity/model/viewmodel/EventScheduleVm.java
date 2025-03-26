package com.winnguyen1905.Activity.model.viewmodel;

import java.time.LocalDateTime;

public record EventScheduleVm(
    Long id,
    Long activityId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String location,
    String description,
    String status
) {} 
