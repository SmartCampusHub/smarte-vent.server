package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

public record ActivityStatistical(
    
) {
  public static record InnerActivityStatistical(
      Instant date,
      String name,
      String description,
      String startDate,
      String endDate,
      String location,
      Integer totalParticipants,
      Integer totalActivities
  )implements AbstractModel {
  }
}
