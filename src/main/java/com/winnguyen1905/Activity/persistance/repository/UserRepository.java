package com.winnguyen1905.Activity.persistance.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EUserCredentials;

@Repository
public interface UserRepository extends JpaRepository<EUserCredentials, Long> {
  Optional<EUserCredentials> findByStudentCode(String studentCode);
  Optional<EUserCredentials> findByRefreshToken(String refreshToken);
}
