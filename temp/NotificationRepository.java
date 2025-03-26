package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.ENotification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<ENotification, Long> {
    List<ENotification> findByCreatedBy(String createdBy);
}
