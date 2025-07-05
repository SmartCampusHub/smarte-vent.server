package com.winnguyen1905.activity.rest.model.dto;

import com.winnguyen1905.activity.common.constant.ParticipationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationUpdateDto implements AbstractModel {
    @NotNull
    private Long participationId;
    
    private ParticipationStatus status;
    
    private String rejectionReason;
    
    private String verifiedNote;
} 
