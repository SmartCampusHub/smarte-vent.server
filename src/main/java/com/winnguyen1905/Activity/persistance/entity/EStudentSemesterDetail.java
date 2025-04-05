package com.winnguyen1905.Activity.persistance.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Constraint;
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
@Table(name = "student_semester_detail")
public class EStudentSemesterDetail {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private EAccountCredentials student;

  @Min(60)
  @Max(100)
  @Column(name = "attendance_score")
  private Integer attendanceScore;

  @Min(0)
  @Max(4)
  @Column(name = "gpa")
  private Float gpa;

  @JsonIgnore
  @Column(name = "created_by", nullable = true)
  private String createdBy;

  @JsonIgnore
  @Column(name = "updated_by", nullable = true)
  private String updatedBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date", updatable = true)
  private Instant updatedDate;
}
