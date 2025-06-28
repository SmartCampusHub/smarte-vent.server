package com.winnguyen1905.activity.persistance.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.activity.common.constant.ReportStatus;
import com.winnguyen1905.activity.common.constant.ReportType;

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
@Table(name = "report")
public class EReport {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @Column(name = "target_type")
  @Enumerated(EnumType.STRING)
  private ReportType reportType;

  @Column(name = "target_id")
  private Long reportedObjectId;

  @Column(name = "title")
  private String title;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String description;

  @ManyToOne
  @JoinColumn(name = "reporter_id")
  private EAccountCredentials reporter;

  // TODO: new attribute
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ReportStatus status;

  @Column(name = "is_reviewed")
  private Boolean isReviewed;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Column(name = "reviewer_id")
  private Long reviewerId;

  @Column(name = "reviewer_response")
  private String reviewerResponse;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;
}
