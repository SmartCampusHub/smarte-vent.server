package com.winnguyen1905.activity.persistance.entity;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

  @OneToMany(mappedBy = "participation")
  private List<EFeedback> feedbacks;

  @Enumerated(EnumType.STRING)
  @Column(name = "attendee_role")
  private ParticipationRole participationRole;

  @JsonIgnore
  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @CreationTimestamp
  @Column(name = "registered_at", updatable = false, columnDefinition = "DATETIME(6)")
  private Instant registeredAt;

  // TODO: new attribute
  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "processed_by")
  private String processedBy;

  @Column(name = "rejection_reason")
  private String rejectionReason;

  @Column(name = "verified_note")
  private String verifiedNote;
}
