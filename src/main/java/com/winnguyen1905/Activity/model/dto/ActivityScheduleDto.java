package com.winnguyen1905.Activity.model.dto;

import java.time.LocalDateTime;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityScheduleDto {
  private Long activityId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String activityDescription;
  private ScheduleStatus status;
  private String location;
}
