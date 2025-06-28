package com.winnguyen1905.activity.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.activity.persistance.entity.ENotification;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<ENotification, Long> {

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
  @Query("SELECT n.notificationType, COUNT(n) FROM ENotification n GROUP BY n.notificationType")
  List<Object[]> getNotificationStatisticsByType();

  /**
   * Get user notification metrics
   */
  @Query("SELECT COUNT(n) FROM ENotification n WHERE n.receiver.id = :userId AND n.createdDate BETWEEN :startDate AND :endDate")
  Long getUserNotificationCount(
      @Param("userId") Long userId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  /**
   * Get notification engagement metrics
   */
  @Query("SELECT COUNT(n) FROM ENotification n WHERE n.notificationType = :notificationType AND n.isRead = true")
  Long getNotificationReadCount(@Param("notificationType") String notificationType);
}
