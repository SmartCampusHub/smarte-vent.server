package com.winnguyen1905.Activity.model.viewmodel;

import java.time.LocalDateTime;

public record ParticipationDetailVm(
    Long id,
    String studentId,
    Long activityId,
    LocalDateTime registrationTime,
    String status,
    String attendanceStatus
) {} 
