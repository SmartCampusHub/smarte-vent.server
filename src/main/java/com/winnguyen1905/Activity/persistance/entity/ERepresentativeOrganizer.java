package com.winnguyen1905.Activity.persistance.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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
@Table(name = "representative_organizer")
public class ERepresentativeOrganizer {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @Column(name = "organization_name")
  private String organizationName;

  @Column(name = "representative_name")
  private String representativeName;

  @Column(name = "representative_phone")
  private String representativePhone;

  @Column(name = "representative_email")
  private String representativeEmail;

  @Column(name = "representative_position")
  private String representativePosition;

  @Column(name = "created_by", nullable = true)
  private Long createdBy;

  @JsonIgnore
  @Column(name = "updated_by", nullable = true)
  private Long updatedBy;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date", updatable = true)
  private Instant updatedDate;

  @OneToMany(mappedBy = "representativeOrganizer")
  private List<EActivity> activities;

  @PrePersist
  public void prePersist() {
    this.createdDate = Instant.now();
    this.updatedDate = Instant.now();
  }
}
