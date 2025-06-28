package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleVm implements AbstractModel {
    private Long id;
    private Long activityId;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private String description;
    private String status;
} 
