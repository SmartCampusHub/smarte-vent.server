package com.winnguyen1905.Activity.persistance.repository.TriggerRepo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.ENotification;

import java.util.List;
import java.util.Map;

// @Repository
public interface NotificationRepository {

  @Query("SELECT n FROM ENotification n WHERE n.receiver = :receiver ORDER BY n.isRead, n.createdDate DESC")
  List<ENotification> findByReceiverOrderByIsReadAscCreatedAtDesc(EAccountCredentials receiver);

  @Query("SELECT n FROM ENotification n WHERE n.receiver = :receiver ORDER BY n.isRead, n.createdDate DESC")
  Page<ENotification> findByReceiverOrderByIsReadAscCreatedAtDesc(EAccountCredentials receiver, Pageable pageable);

  @Deprecated
  default List<ENotification> findByReceiver(EAccountCredentials receiver) {
    return findByReceiverOrderByIsReadAscCreatedAtDesc(receiver);
  }

  @Query("SELECT a FROM ENotification a WHERE a.receiver.id = :id")
  Page<ENotification> findAllByReceiverId(@Param("id") Long id, Pageable pageable);

  /**
   * Get notification statistics by type
   */
  @Query(value = "CALL get_notification_statistics_by_type()", nativeQuery = true)
  List<Map<String, Object>> getNotificationStatisticsByType();

  /**
   * Get user notification metrics
   */
  @Query(value = "CALL get_user_notification_metrics(:userId, :startDate, :endDate)", nativeQuery = true)
  Map<String, Object> getUserNotificationMetrics(
      @Param("userId") Long userId,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate
  );

  /**
   * Get notification engagement metrics
   */
  @Query(value = "CALL get_notification_engagement_metrics(:notificationType)", nativeQuery = true)
  List<Map<String, Object>> getNotificationEngagementMetrics(
      @Param("notificationType") String notificationType
  );
}
