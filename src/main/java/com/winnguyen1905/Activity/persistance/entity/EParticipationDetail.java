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
@Table(name = "attendance")
public class EParticipationDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "attendee_id")
  private EAccountCredentials participant;

  @ManyToOne
  @JoinColumn(name = "activity_id")
  private EActivity activity;

  @Enumerated(EnumType.STRING)
  @Column(name = "aattendance_status")
  private ParticipationStatus participationStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "attendee_role")
  private ParticipationRole participationRole;

  @ManyToOne
  @JoinColumn(name = "verified_by")
  private EAccountCredentials verifiedByAccount;

  @JsonIgnore
  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @CreationTimestamp
  @Column(name = "registered_at", updatable = false, columnDefinition = "DATETIME(6)")
  private Instant registeredAt;
}
