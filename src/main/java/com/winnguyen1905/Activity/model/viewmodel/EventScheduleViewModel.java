package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;
import java.time.Instant;
import com.winnguyen1905.activity.common.constant.ScheduleStatus;
import com.winnguyen1905.activity.model.dto.AbstractModel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventScheduleViewModel  implements AbstractModel {
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
