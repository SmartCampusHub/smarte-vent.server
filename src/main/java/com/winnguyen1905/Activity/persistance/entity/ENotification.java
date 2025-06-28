package com.winnguyen1905.activity.persistance.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.activity.common.constant.NotificationType;

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
@Table(name = "notification")
public class ENotification {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  protected Long id;

  @ManyToOne
  @JoinColumn(name = "receiver_id")
  private EAccountCredentials receiver;

  @Column(name = "title")
  private String title;

  @Column(name = "is_read")
  private Boolean isRead;

  @Column(name = "is_deleted")
  private Boolean isDeleted;

  @ManyToOne
  @JoinColumn(name = "sender_id", nullable = true)
  private EAccountCredentials sender;

  @Enumerated(EnumType.STRING)
  @Column(name = "notification_type")
  private NotificationType notificationType;

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @CreationTimestamp
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;
}
