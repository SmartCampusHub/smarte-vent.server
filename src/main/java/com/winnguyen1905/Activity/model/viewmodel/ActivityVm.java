package com.winnguyen1905.Activity.model.viewmodel;

import java.time.LocalDateTime;

public record ActivityVm(
    Long id,
    String title,
    String description,
    String location,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Integer maxParticipants,
    String status,
    Long categoryId,
    Long classId) {
}
