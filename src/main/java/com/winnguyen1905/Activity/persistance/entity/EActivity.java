package com.winnguyen1905.Activity.persistance.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.Activity.common.constant.ActivityCategory;
import com.winnguyen1905.Activity.common.constant.ActivityStatus;
import com.winnguyen1905.Activity.common.constant.MajorType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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
  @Column(name = "version")
  private Long version;

  @Id
  // @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  protected Long id;

  @Column(name = "attendance_score")
  private Integer attendanceScoreUnit;

  @Column(name = "name")
  private String activityName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "short_description ", columnDefinition = "TEXT")
  private String shortDescription;

  @ElementCollection
  @Column(name = "tags")
  private List<String> tags;

  @ManyToOne
  @JoinColumn(name = "organization_id")
  private EOrganization organization;

  @Enumerated(EnumType.STRING)
  @Column(name = "category")
  private ActivityCategory activityCategory;

  @Column(name = "max_attendees")
  private Integer capacityLimit;

  @Column(name = "current_participants")
  private Integer currentParticipants;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
  private List<EParticipationDetail> participationDetails;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
  private List<EActivitySchedule> activitySchedules;

  @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
  private List<EFeedback> feedbacks;

  @Column(name = "start_date")
  private Instant startDate;

  @Column(name = "end_date")
  private Instant endDate;

  @Column(name = "venue")
  private String venue;

  @Column(name = "address")
  private String address;

  private Double latitude;

  private Double longitude;

  @Column(name = "online_link")
  private String onlineLink;

  @Column(name = "fee")
  private Double fee;

  @Column(name = "is_featured")
  private Boolean isFeatured;

  @Column(name = "is_approved")
  private Boolean isApproved;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "likes")
  private Integer likes;

  @Column(name = "registration_deadline ", updatable = true)
  private Instant registrationDeadline;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ActivityStatus status;

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

  @PrePersist
  private void prePersist() {
    if (this.id == null) {
      this.id = 2000 + (long) (Math.random() * 1000);
    }
  }
}
