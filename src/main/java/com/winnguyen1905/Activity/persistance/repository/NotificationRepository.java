package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EActivity;
import com.winnguyen1905.Activity.persistance.entity.ENotification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<ENotification, Long> {

  @Query("SELECT a FROM ENotification a WHERE a.receiver.id = :id")
  Page<ENotification> findAllByReceiverId(@Param("id") Long id, Pageable pageable);
}
