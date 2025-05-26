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
@Table(name = "report")
public class EReport {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "activity_id")
  private EActivity activity;

  @Column(name = "report_type")
  private String reportType;

  @ManyToOne
  @JoinColumn(name = "reporter_id")
  private EAccountCredentials reporter;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @JsonIgnore
  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;
}
