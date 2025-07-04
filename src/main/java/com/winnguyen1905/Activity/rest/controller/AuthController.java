package com.winnguyen1905.activity.rest.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.activity.auth.CookieUtils;
import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.SystemConstant;
import com.winnguyen1905.activity.exception.ResourceNotFoundException;
import com.winnguyen1905.activity.model.dto.ChangePasswordDto;
import com.winnguyen1905.activity.model.dto.LoginRequest;
import com.winnguyen1905.activity.model.dto.RegisterRequest;
import com.winnguyen1905.activity.model.viewmodel.AccountVm;
import com.winnguyen1905.activity.model.viewmodel.AuthResponse;
import com.winnguyen1905.activity.rest.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Login to the system", description = "Authenticate a user and return access and refresh tokens")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successful login", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
  })
  public ResponseEntity<AuthResponse> login(
      @Parameter(description = "Login credentials", required = true) @RequestBody LoginRequest loginRequest) {
    AuthResponse authResponse = this.authService.login(loginRequest);
    return ResponseEntity
        .ok()
        .header(
            HttpHeaders.SET_COOKIE, CookieUtils
                .createCookie(SystemConstant.REFRESH_TOKEN, authResponse.getRefreshToken())
                .toString())
        .body(authResponse);
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user", description = "Create a new user account")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "User registered successfully", 
                 content = @Content(schema = @Schema(implementation = AccountVm.class))),
    @ApiResponse(responseCode = "400", description = "Invalid input data"),
    @ApiResponse(responseCode = "409", description = "User already exists")
  })
  public ResponseEntity<AccountVm> register(
      @Parameter(description = "Registration details", required = true) @RequestBody RegisterRequest registerRequest) {
    AccountVm accountVm = authService.register(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(accountVm);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh access token", description = "Get a new access token using a refresh token")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully", 
                 content = @Content(schema = @Schema(implementation = AuthResponse.class))),
    @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
    @ApiResponse(responseCode = "404", description = "Refresh token not found")
  })
  public ResponseEntity<AuthResponse> getAuthenticationResultByRefreshToken(
      @Parameter(description = "Refresh token from cookie", required = true) 
      @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
    if (refreshToken.isEmpty()) {
      throw new ResourceNotFoundException("Not found refresh token");
    }
    AuthResponse auth = this.authService.refreshToken(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE,
            CookieUtils.createCookie(SystemConstant.REFRESH_TOKEN, auth.getRefreshToken())
                .toString())
        .body(auth);
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout the user", description = "Invalidate the user's refresh token")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successfully logged out"),
    @ApiResponse(responseCode = "404", description = "Refresh token not found")
  })
  public ResponseEntity<Void> logout(
      @Parameter(description = "Refresh token from cookie") 
      @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken) {
    if (refreshToken.isEmpty()) {
      throw new ResourceNotFoundException("Not found refresh token");
    }
    this.authService.logout(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, CookieUtils.deleteCookie(SystemConstant.REFRESH_TOKEN).toString())
        .build();
  }

  @PostMapping("/change-password")
  @Operation(summary = "Change password", description = "Change the password for the current user")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Password changed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid password data"),
    @ApiResponse(responseCode = "401", description = "Invalid current password")
  })
  public ResponseEntity<Void> changePassword(
      @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
      @Parameter(description = "Password change details", required = true) @RequestBody ChangePasswordDto changePasswordDto) {
    this.authService.changePassword(accountRequest, changePasswordDto);
    return ResponseEntity.ok().build();
  }
}
