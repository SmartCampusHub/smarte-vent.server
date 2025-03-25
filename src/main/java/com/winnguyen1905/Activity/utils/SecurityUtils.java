package com.winnguyen1905.Activity.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnguyen1905.Activity.auth.AuthPermission;

public final class SecurityUtils {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    private static List<AuthPermission> jsonToPermission(List<String> permissionStrings) {
        ObjectMapper objectMapper = new ObjectMapper();
        return permissionStrings.stream()
            .map(permissionString -> permissionString.trim().replaceAll("^\"|\"$", "").replaceAll("^\\{|\\}$", ""))
            .map(trimmedString -> {
                Map<String, String> permissionMap = Arrays.stream(trimmedString.split(",(?![^\\{\\[]*[\\]\\}])"))
                    .map(pair -> pair.split("=", 2))
                    .filter(entry -> entry.length == 2)
                    .collect(Collectors.toMap(
                        entry -> entry[0].trim(),
                        entry -> entry[1].trim()));
                return objectMapper.convertValue(permissionMap, AuthPermission.class);
            })
            .collect(Collectors.toList());
    }

    public static List<AuthPermission> getCurrentUsersPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return extractPermissionsFromAuthentication(authentication);
    }

    private static List<AuthPermission> extractPermissionsFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            return jsonToPermission(permissions);
        }
        return Collections.emptyList();
    }

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String subject = jwt.getSubject();
            int separatorIndex = subject.indexOf("/") + 1;
            if (separatorIndex > 0 && separatorIndex < subject.length()) {
                try {
                    return UUID.fromString(subject.substring(separatorIndex));
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public static String getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return extractPrincipal(authentication);
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

    public static String getCurrentUserJWT() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }

    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return getAuthorities(authentication)
                .anyMatch(authority -> Arrays.asList(authorities).contains(authority));
        }
        return false;
    }

    public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
        return !hasCurrentUserAnyOfAuthorities(authorities);
    }

    public static boolean hasCurrentUserThisAuthority(String authority) {
        return hasCurrentUserAnyOfAuthorities(authority);
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
    }
}
