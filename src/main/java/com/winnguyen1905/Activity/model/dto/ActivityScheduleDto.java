package com.winnguyen1905.Activity.model.dto;

import java.time.Instant;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityScheduleDto implements AbstractModel {
  private Long activityId;
  private Instant startTime;
  private Instant endTime;
  private String activityDescription;
  private ScheduleStatus status;
  private String location;
}
