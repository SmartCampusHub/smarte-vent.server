package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;
import java.time.LocalDateTime;

import com.winnguyen1905.Activity.common.constant.ActivityStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityViewModel {
    private Long id;
    private String activityName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String activityVenue;
    private Integer capacity;
    private ActivityStatus activityStatus;
    private String attendanceScoreUnit;
    private Long categoryId;
    private String categoryName;
    private Long organizerId;
    private String organizerName;
    private Instant createdDate;
    private Instant updatedDate;
} 
