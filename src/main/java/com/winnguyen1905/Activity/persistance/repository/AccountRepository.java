package com.winnguyen1905.Activity.persistance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;

@Repository
public interface AccountRepository
    extends JpaRepository<EAccountCredentials, Long>, JpaSpecificationExecutor<EAccountCredentials> {
  Optional<EAccountCredentials> findByIdentifyCode(String identifyCode);
  Optional<EAccountCredentials> findByRefreshToken(String refreshToken);
}
