package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;
import java.time.Instant;
import com.winnguyen1905.Activity.common.constant.ScheduleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleViewModel {
  private Long id;
  private Long activityId;
  private String activityTitle;
  private Instant startTime;
  private Instant endTime;
  private String activityDescription;
  private ScheduleStatus status;
  private String location;
  private String createdBy;
  private String updatedBy;
  private Instant createdDate;
  private Instant updatedDate;
}
