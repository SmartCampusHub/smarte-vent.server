package com.winnguyen1905.Activity.model.dto;

import java.time.LocalDateTime;

public record ParticipationDetailDto(
    String studentId,
    Long activityId,
    LocalDateTime registrationTime,
    String status,
    String attendanceStatus
) {} 
