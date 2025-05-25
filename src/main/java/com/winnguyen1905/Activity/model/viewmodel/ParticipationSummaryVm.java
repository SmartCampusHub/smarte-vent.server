package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationSummaryVm implements AbstractModel {
  private Long participationId;
  private Long activityId;
  private String activityName;
  private ActivityCategory activityCategory;
  private ParticipationRole participationRole;
  private ParticipationStatus participationStatus;
  private Instant participationDate;
  private Double hoursSpent;
  private Double assessmentScore; // From feedback if available
}
