package com.winnguyen1905.Activity.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import com.winnguyen1905.Activity.exception.BaseException;
import com.winnguyen1905.Activity.utils.SecurityUtils;


@Configuration
public class JwtConfig {
  @Value("${jwt.base64-secret}")
  private String jwtKey;

  public SecretKey secretKey() {
    byte[] keyBytes = Base64.from(this.jwtKey).decode();
    return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtils.JWT_ALGORITHM.getName());
  }

  @Bean
  JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey()));
  }

  // @Bean
  // public JwtDecoder jwtDecoder() {
  //   return NimbusJwtDecoder.withSecretKey(secretKey()).macAlgorithm(SecurityUtils.JWT_ALGORITHM).build();
  // }

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
        .withSecretKey(secretKey())
        .macAlgorithm(SecurityUtils.JWT_ALGORITHM)
        .build();
    System.out.println(jwtKey);
    return token -> {
      try {
        System.out.println(jwtKey);
        return nimbusJwtDecoder.decode(token);
      } catch (Exception e) {
        System.out.println("Token error: " + token);
        throw new BaseException("refresh token invalid", 401);
      }
    };
  }

  // @Bean
  // JwtUtils jwtUtils() {
  // return new JwtUtils();
  // }

  // @Bean
  // JwtAuthenticationConverter jwtAuthenticationConverter() {
  // JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new
  // JwtGrantedAuthoritiesConverter();
  // grantedAuthoritiesConverter.setAuthorityPrefix("");
  // grantedAuthoritiesConverter.setAuthoritiesClaimName("permission");
  // JwtAuthenticationConverter jwtAuthenticationConverter = new
  // JwtAuthenticationConverter();
  // jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
  // return jwtAuthenticationConverter;
  // }

  // @Bean
  // public JwtAuthenticationConverter jwtAuthenticationConverter() {
  //   JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
  //   converter.setJwtGrantedAuthoritiesConverter(jwt -> {
  //     List<String> roles = jwt.getClaimAsStringList("roles");
  //     return Flux.fromIterable(roles.stream()
  //         .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
  //         .collect(Collectors.toList()));
  //   });
  //   return converter;
  // }
}
