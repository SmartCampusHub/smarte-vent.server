package com.winnguyen1905.Activity.config;

import java.util.UUID;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import com.winnguyen1905.Activity.auth.CustomUserDetails;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.AccountRole;

@Configuration
public class AccountRequestConfig {
  @Bean
  Function<CustomUserDetails, TAccountRequest> fetchUser() {
    return (principal -> {
      return TAccountRequest.builder()
          .id(principal.id())
          .role(principal.getAuthorities().stream().findFirst().get().getAuthority().equals("ROLE_ADMIN")
              ? AccountRole.ADMIN
              : AccountRole.STUDENT)
          .build();
    });
  }
}
