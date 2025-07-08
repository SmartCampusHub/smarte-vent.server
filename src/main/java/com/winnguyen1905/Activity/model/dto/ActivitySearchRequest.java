package com.winnguyen1905.activity.model.dto;

import java.time.Instant;
import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySearchRequest implements AbstractModel {
  private String activityName;
  private ActivityCategory activityCategory;
  private ActivityStatus activityStatus;
  private String organizationName;
  private Instant startDateFrom;
  private Instant startDateTo;
  private Instant endDateFrom;
  private Instant endDateTo;
  private Integer minAttendanceScoreUnit;
  private Integer maxAttendanceScoreUnit;
  private Integer minCapacityLimit;
  private Integer maxCapacityLimit;
  private Boolean isApproved;
  private String activityVenue;
}
