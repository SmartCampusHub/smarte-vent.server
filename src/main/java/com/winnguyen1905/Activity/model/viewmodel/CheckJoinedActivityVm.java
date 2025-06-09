package com.winnguyen1905.Activity.model.viewmodel;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import jakarta.persistence.Column;
import lombok.Builder;

@Builder
public record CheckJoinedActivityVm(Boolean isJoined, ParticipationStatus status, ParticipationRole role,
    Instant registeredAt,
    Instant processedAt,
    String processedBy,
    String rejectionReason,
    String verifiedNote) implements AbstractModel {

  @Builder
  public CheckJoinedActivityVm(Boolean isJoined, ParticipationStatus status, ParticipationRole role,
      Instant registeredAt, Instant processedAt, String processedBy, String rejectionReason, String verifiedNote) {
    this.isJoined = isJoined;
    this.status = status;
    this.role = role;
    this.registeredAt = registeredAt;
    this.processedAt = processedAt;
    this.processedBy = processedBy;
    this.rejectionReason = rejectionReason;
    this.verifiedNote = verifiedNote;
  }
}
