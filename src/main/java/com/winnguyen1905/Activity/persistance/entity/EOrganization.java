package com.winnguyen1905.Activity.persistance.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.OrganizationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "organization")
public class EOrganization {
  @Id
  // @GeneratedValue(strategy = GenerationType.AUTO)
  // @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "phone")
  private String phone;

  @Column(name = "email")
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private OrganizationType type;

  @UpdateTimestamp
  @Column(name = "updated_date", updatable = true)
  private Instant updatedDate;

  @OneToMany(mappedBy = "organization")
  private List<EActivity> activities;

  @Column(name = "description")
  private String description;

  @MapsId
  @OneToOne
  @JoinColumn(name = "id")
  private EAccountCredentials account;

  @PrePersist
  public void prePersist() {
    this.updatedDate = Instant.now();
  }
}
