package com.winnguyen1905.Activity.persistance.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.Activity.common.constant.ParticipationRole;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "participation_detail")
public class EParticipationDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "participant_id")
  private EAccountCredentials participant;

  @ManyToOne
  @JoinColumn(name = "activity_id")
  private EActivity activity;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ParticipationStatus participationStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "participation_role")
  private ParticipationRole participationRole;

  @Column(name = "qr_code")
  private String qrCode;

  @JsonIgnore
  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @CreationTimestamp
  @Column(name = "registered_at", updatable = false, columnDefinition = "DATETIME(6)")
  private Instant registeredAt;
}
