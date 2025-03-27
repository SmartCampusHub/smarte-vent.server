package com.winnguyen1905.Activity.persistance.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(name = "confirmation")
public class EConfirmation {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "participation_id")
  private EParticipationDetail participation;

  @Min(0)
  @Max(10)
  @Column(name = "rating")
  private Double rating;

  @Column(name = "feedback_description", columnDefinition = "TEXT")
  private String feedbackDescription;

  @Column(name = "confirmed_at")
  private Instant confirmedAt;

  @ManyToOne
  @JoinColumn(name = "confirmed_by_account_id")
  private EAccountCredentials confirmedByAccount;

  @Column(name = "feedback_created_at")
  private Instant feedbackCreatedAt;

  @JsonIgnore
  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;
}
