package com.winnguyen1905.activity.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.winnguyen1905.activity.auth.CustomUserDetails;
import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.rest.model.viewmodel.TokenPair;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:application.yaml")
public class JwtUtils {
  public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  @Value("${jwt.access_token-validity-in-seconds}")
  private String jwtAccessTokenExpiration;

  @Value("${jwt.refresh_token-validity-in-seconds}")
  private String jwtRefreshTokenExpiration;

  public String extractUsername(String token) {
    return jwtDecoder.decode(token).getSubject().split("/")[0];
  }

  public UserDetails getUserDetailsFromToken(String token) {
    var jwt = jwtDecoder.decode(token);
    var claims = jwt.getClaims();
    return CustomUserDetails.builder()
        .id(Long.parseLong(claims.get("id").toString()))
        .username(claims.get("username").toString())
        .password(claims.get("password").toString())
        .role(AccountRole.valueOf(claims.get("role").toString()))
        .build();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return jwtDecoder.decode(token).getExpiresAt().isBefore(Instant.now());
  }

  public JwtClaimsSet createJwtClaimsSet(CustomUserDetails userDetails, Instant now, Instant validity) {
    return JwtClaimsSet.builder()
        .issuedAt(now)
        .expiresAt(validity)
        .subject(userDetails.id().toString())
        .claim("id", userDetails.id())
        .claim("username", userDetails.getUsername())
        .claim("role", userDetails.role().toString())
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
        .build();
  }
}
