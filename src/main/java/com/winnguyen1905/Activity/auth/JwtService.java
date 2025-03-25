package com.winnguyen1905.Activity.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.winnguyen1905.Activity.model.viewmodel.TokenPair;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@PropertySource("classpath:application.yaml")
public class JwtService {
  public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

  private final JwtEncoder jwtEncoder;
  private final String jwtAccessTokenExpiration;
  private final String jwtRefreshTokenExpiration;

  public JwtService(JwtEncoder jwtEncoder,
      @Value("${jwt.access_token-validity-in-seconds}") String jwtAccessTokenExpiration,
      @Value("${jwt.refresh_token-validity-in-seconds}") String jwtRefreshTokenExpiration) {
    this.jwtEncoder = jwtEncoder;
    this.jwtAccessTokenExpiration = jwtAccessTokenExpiration;
    this.jwtRefreshTokenExpiration = jwtRefreshTokenExpiration;
  }

  private JwtClaimsSet createJwtClaimsSet(CustomUserDetails userDetails, Instant now, Instant validity) {
    // List<AuthPermission> permissions = userDetails.role() instanceof AuthRole role
    //     ? role.getPermissions()
    //     : List.of();

    return JwtClaimsSet.builder()
        .issuedAt(now)
        .expiresAt(validity)
        .subject(userDetails.username() + "/" + userDetails.id())
        .claim("user", userDetails)
        .claim("permissions", null)
        .build();
  }

  public TokenPair createTokenPair(CustomUserDetails userDetails) {
    Instant now = Instant.now();
    Instant accessTokenValidity = now.plus(Long.parseLong(jwtAccessTokenExpiration), ChronoUnit.SECONDS);
    Instant refreshTokenValidity = now.plus(Long.parseLong(jwtRefreshTokenExpiration), ChronoUnit.SECONDS);

    JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
    String accessToken = jwtEncoder.encode(JwtEncoderParameters
        .from(jwsHeader, createJwtClaimsSet(userDetails, now, accessTokenValidity)))
        .getTokenValue();
    String refreshToken = jwtEncoder.encode(JwtEncoderParameters
        .from(jwsHeader, createJwtClaimsSet(userDetails, now, refreshTokenValidity)))
        .getTokenValue();

    return TokenPair.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(Integer.parseInt(jwtAccessTokenExpiration))
        .build();
  }
}
