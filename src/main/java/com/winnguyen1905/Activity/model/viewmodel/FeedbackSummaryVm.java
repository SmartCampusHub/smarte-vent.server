package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSummaryVm implements AbstractModel {
    private Long id;
    private Long activityId;
    private String activityName;
    private Long studentId;
    private String studentName;
    private Double rating;
    private Instant createdDate;
}
