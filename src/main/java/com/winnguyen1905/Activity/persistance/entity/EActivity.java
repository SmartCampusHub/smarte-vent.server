package com.winnguyen1905.Activity.persistance.entity;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Version;
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
@Table(name = "activity")
public class EActivity {
  @Version
  private Long version;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @Column(name = "attendance_score_unit")
  private Integer attendanceScoreUnit;

  @Column(name = "activity_name")
  private String activityName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @OneToMany(mappedBy = "activity")
  private List<EReport> reports;

  @ManyToOne
  @JoinColumn(name = "representative_organizer_id")
  private ERepresentativeOrganizer representativeOrganizer;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_category")
  private ActivityCategory activityCategory;
  
  @Column(name = "activity_capacity_limit", columnDefinition = "TEXT")
  private Integer capacityLimit;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
  private List<EParticipationDetail> participationDetails;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
  private List<EActivitySchedule> activitySchedules;

  @Column(name = "start_date")
  private Instant startDate;

  @Column(name = "end_date")
  private Instant endDate;

  @Column(name = "activity_venue")
  private String activityVenue;

  @Column(name = "capacity")
  private Integer capacity;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_status")
  private ActivityStatus activityStatus;

  @JsonIgnore
  @Column(name = "created_by_id", nullable = true)
  private Long createdById;

  @JsonIgnore
  @Column(name = "updated_by_id", nullable = true)
  private Long updatedById;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date", updatable = true)
  private Instant updatedDate;
}
