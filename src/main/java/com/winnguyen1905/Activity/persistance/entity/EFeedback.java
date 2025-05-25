package com.winnguyen1905.Activity.persistance.entity;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.Activity.common.constant.NotificationType;

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
@Table(name = "notification")
public class EFeedback {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  protected Long id;

  @Column(name = "feedback_description", columnDefinition = "TEXT")
  private String feedbackDescription;

  @ManyToOne
  @JoinColumn(name = "activity_id")
  private EActivity activity;

  @Min(0)
  @Max(10)
  @Column(name = "rating")
  private Double rating;

  @ManyToOne
  @JoinColumn(name = "pariticipation_id")
  private EParticipationDetail participation;
}
