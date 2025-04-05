package com.winnguyen1905.Activity.rest.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.auth.CookieUtils;
import com.winnguyen1905.Activity.common.constant.SystemConstant;
import com.winnguyen1905.Activity.exception.ResourceNotFoundException;
import com.winnguyen1905.Activity.model.dto.LoginRequest;
import com.winnguyen1905.Activity.model.dto.RegisterRequest;
import com.winnguyen1905.Activity.model.viewmodel.AuthResponse;
import com.winnguyen1905.Activity.rest.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
    AuthResponse authResponse = this.authService.login(loginRequest);
    return ResponseEntity
        .ok()
        .header(
            HttpHeaders.SET_COOKIE, CookieUtils
                .createCookie(SystemConstant.REFRESH_TOKEN, authResponse.refreshToken())
                .toString())
        .body(authResponse);
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
    authService.register(registerRequest);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> getAuthenticationResultByRefreshToken(
      @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
    if (refreshToken.isEmpty()) {
      throw new ResourceNotFoundException("Not found refresh token");
    }
    AuthResponse auth = this.authService.refreshToken(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtils.createCookie(SystemConstant.REFRESH_TOKEN, auth.refreshToken())
                .toString())
        .body(auth);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
    if (refreshToken.isEmpty()) {
      throw new ResourceNotFoundException("Not found refresh token");
    }
    this.authService.logout(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, CookieUtils.deleteCookie(SystemConstant.REFRESH_TOKEN).toString())
        .build();
  }
}
