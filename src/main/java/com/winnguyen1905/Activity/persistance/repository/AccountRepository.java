package com.winnguyen1905.Activity.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;

@Repository
public interface AccountRepository extends JpaRepository<EAccountCredentials, Long> {
  Optional<EAccountCredentials> findByStudentCode(String studentCode);
  Optional<EAccountCredentials> findByRefreshToken(String refreshToken);
}
