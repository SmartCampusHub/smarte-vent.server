package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;
import java.util.List;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.winnguyen1905.activity.common.constant.ScheduleStatus;
import com.winnguyen1905.activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityScheduleVm implements AbstractModel {
  private Long id;
  private Long activityId;
  private String activityName;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant startTime;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant endTime;
  private String activityDescription;
  private ScheduleStatus status;
  private String location;
  private String createdBy;
  private String updatedBy;
  private Instant createdDate;
  private Instant updatedDate;
}
