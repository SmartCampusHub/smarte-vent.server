package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;
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
public class CheckJoinedActivityVm implements AbstractModel {
    private Boolean isJoined;
    private ParticipationStatus status;
    private ParticipationRole role;
    private Instant registeredAt;
    private Instant processedAt;
    private String processedBy;
    private String rejectionReason;
    private String verifiedNote;
}
