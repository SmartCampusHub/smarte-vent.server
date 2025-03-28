package com.winnguyen1905.Activity.model.viewmodel;

import java.time.LocalDateTime;
import java.util.List;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVm implements AbstractModel{
  private Long id;
  private String activityName;
  private String description;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String activityVenue;
  private Integer capacity;
  private ActivityStatus activityStatus;
  private String activityType;
  private ActivityCategory activityCategory;
  private String activityDescription;
  private String activityImage;
  private String activityLink;
  private String attendanceScoreUnit;
  private Long representativeOrganizerId;
  private List<ActivityScheduleVm> activitySchedules;
}
