package com.winnguyen1905.Activity.auth;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class SecurityUtils {

  public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

  private static Flux<AuthPermission> jsonToPermission(List<String> permissionStrings) {
    ObjectMapper objectMapper = new ObjectMapper();
    return Flux.fromIterable(permissionStrings)
        .map(permissionString -> permissionString.trim().replaceAll("^\"|\"$", "").replaceAll("^\\{|\\}$", ""))
        .map(trimmedString -> {
          Map<String, String> permissionMap = Arrays.stream(trimmedString.split(",(?![^\\{\\[]*[\\]\\}])"))
              .map(pair -> pair.split("=", 2))
              .filter(entry -> entry.length == 2)
              .collect(Collectors.toMap(
                  entry -> entry[0].trim(),
                  entry -> entry[1].trim()));
          return objectMapper.convertValue(permissionMap, AuthPermission.class);
        });
  }

  

  public static Flux<AuthPermission> getCurrentUsersPermissions() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMapMany(SecurityUtils::extractPermissionsFromAuthentication)
        .switchIfEmpty(Flux.error(new IllegalStateException("No permissions found for the current user")));
  }

  private static Flux<AuthPermission> extractPermissionsFromAuthentication(Authentication authentication) {
    if (authentication.getPrincipal() instanceof Jwt jwt) {
      List<String> permissions = jwt.getClaimAsStringList("permissions");
      return jsonToPermission(permissions);
    }
    return Flux.empty();
  }

  public static Mono<UUID> getCurrentUserId() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMap(authentication -> {
          if (authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            int separatorIndex = subject.indexOf("/") + 1;
            if (separatorIndex > 0 && separatorIndex < subject.length()) {
              try {
                return Mono.just(UUID.fromString(subject.substring(separatorIndex)));
              } catch (IllegalArgumentException e) {
                return Mono.empty();
              }
            }
          }
          return Mono.empty();
        });
  }

  public static Mono<String> getCurrentUserLogin() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(SecurityUtils::extractPrincipal)
        .defaultIfEmpty(null);
  }

  private static String extractPrincipal(Authentication authentication) {
    if (authentication == null) {
      return null;
    } else if (authentication.getPrincipal() instanceof Jwt jwt) {
      return jwt.getSubject().substring(0, jwt.getSubject().indexOf("/"));
    } else if (authentication.getPrincipal() instanceof String str) {
      return str;
    }
    return null;
  }

  public static Mono<String> getCurrentUserJWT() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(authentication -> authentication.getCredentials() instanceof String)
        .map(authentication -> (String) authentication.getCredentials());
  }

  public static Mono<Boolean> hasCurrentUserAnyOfAuthorities(String... authorities) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(authentication -> getAuthorities(authentication)
            .anyMatch(authority -> Arrays.asList(authorities).contains(authority)))
        .defaultIfEmpty(false);
  }

  public static Mono<Boolean> hasCurrentUserNoneOfAuthorities(String... authorities) {
    return hasCurrentUserAnyOfAuthorities(authorities).map(hasAny -> !hasAny);
  }

  public static Mono<Boolean> hasCurrentUserThisAuthority(String authority) {
    return hasCurrentUserAnyOfAuthorities(authority);
  }

  private static Stream<String> getAuthorities(Authentication authentication) {
    return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
  }
}
