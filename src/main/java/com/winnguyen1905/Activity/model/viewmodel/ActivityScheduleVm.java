package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;
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
public class ActivityScheduleVm {
  private Long id;
  private Long activityId;
  private String activityName;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String activityDescription;
  private ScheduleStatus status;
  private String location;
  private String createdBy;
  private String updatedBy;
  private Instant createdDate;
  private Instant updatedDate;
}
