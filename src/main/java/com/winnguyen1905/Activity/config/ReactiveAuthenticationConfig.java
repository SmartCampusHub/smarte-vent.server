package com.winnguyen1905.Activity.config;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.winnguyen1905.Activity.auth.CustomUserDetails;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;

@Configuration
public class ReactiveAuthenticationConfig {

  // @Bean
  // public JwtAuthenticationConverter jwtAuthenticationConverter() {
  //   JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
  //   converter.setJwtGrantedAuthoritiesConverter(jwt -> {
  //     List<String> roles = Optional.ofNullable(jwt.getClaimAsStringList("roles"))
  //         .filter(list -> !list.isEmpty())
  //         .orElseGet(() -> { 
  //           String roleString = jwt.getClaimAsString("roles");
  //           return roleString != null && !roleString.isBlank()
  //               ? List.of(roleString)
  //               : List.of("ADMIN");
  //         });

  //     return roles.stream()
  //         .filter(Objects::nonNull)
  //         .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
  //         .collect(Collectors.toList());
  //   });
  //   return converter;
  // }

  @Bean
  AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authProvider);
  }

  @Bean("userDetailsService")
  public UserDetailsService userDetailsService(AccountRepository userRepository) {
    return studentCode -> {
      return userRepository.findByStudentCode(studentCode)  
          .map(user -> CustomUserDetails.builder()
              .id(user.getId())
              .role(user.getRole())
              .email(user.getEmail())
              .phone(user.getPhone())
              .username(user.getStudentCode())
              .password(user.getPassword())
              .build())
          .orElseThrow(
            () -> new UsernameNotFoundException("Not found user by username " + studentCode)
            );
    };
  }
}
