package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.winnguyen1905.activity.common.constant.ActivityCategory;
import com.winnguyen1905.activity.common.constant.ActivityStatus;
import com.winnguyen1905.activity.common.constant.MajorType;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationDetailVm implements AbstractModel {
    private Long id;
    private Long studentId;
    private String identifyCode;
    private String participantName;
    private Long activityId;
    private String activityName;
    private ActivityCategory activityCategory;
    private MajorType major;
    private String activityVenue;
    @JsonFormat(shape = JsonFormat.Shape.STRING) 
    private Instant startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING) 
    private Instant endDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING) 
    private Instant registrationTime;
    private ActivityStatus activityStatus;
    private ParticipationStatus participationStatus;
    private ParticipationRole participationRole;
    @JsonFormat(shape = JsonFormat.Shape.STRING) 
    private Instant processedAt;
    private String processedBy;
    private String rejectionReason;
    private String verifiedNote;
}
