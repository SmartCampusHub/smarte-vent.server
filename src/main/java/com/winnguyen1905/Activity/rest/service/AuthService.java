package com.winnguyen1905.Activity.rest.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.winnguyen1905.Activity.auth.CustomUserDetails;
import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.model.dto.LoginRequest;
import com.winnguyen1905.Activity.model.dto.RegisterRequest;
import com.winnguyen1905.Activity.model.viewmodel.AccountVm;
import com.winnguyen1905.Activity.model.viewmodel.AuthResponse;
import com.winnguyen1905.Activity.model.viewmodel.TokenPair;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.utils.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final JwtUtils jwtUtils;
  private final AccountRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public AuthResponse login(LoginRequest loginRequest) {
    CustomUserDetails userDetails = (CustomUserDetails) authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.studentCode(), loginRequest.password())).getPrincipal();

    TokenPair tokenPair = jwtUtils.createTokenPair(userDetails);
    EAccountCredentials userCredentials = userRepository.findById(userDetails.id())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    userCredentials.setRefreshToken(tokenPair.refreshToken());
    userRepository.save(userCredentials);

    return AuthResponse.builder()
        .refreshToken(tokenPair.refreshToken())
        .accessToken(tokenPair.accessToken())
        .account(AccountVm.builder()
            .studentCode(userCredentials.getStudentCode())
            .role(userCredentials.getRole())
            .name(userCredentials.getFullName())
            .id(userCredentials.getId()).build())
        .build();
  }

  public void register(RegisterRequest registerRequest) {
    validateRegisterRequest(registerRequest);

    Optional<EAccountCredentials> user = userRepository.findByStudentCode(registerRequest.studentCode());

    if (user.isPresent()) {
      throw new IllegalArgumentException("Student code is already taken");
    }

    EAccountCredentials newUser = createNewUser(registerRequest);

    userRepository.save(newUser);
  }

  private void validateRegisterRequest(RegisterRequest request) {
    if (request == null || request.studentCode() == null || request.password() == null) {
      throw new IllegalArgumentException("Student code and password are required");
    }
  }

  private EAccountCredentials createNewUser(RegisterRequest request) {
    return EAccountCredentials.builder()
        .email(request.email())
        .phone(request.phone())
        .fullName(request.fullName())
        .isActive(true)
        .refreshToken(null)
        .role(AccountRole.STUDENT)
        .studentCode(request.studentCode())
        .password(passwordEncoder.encode(request.password()))
        .role(AccountRole.STUDENT) // Default role is STUDENT
        .build();
  }

  private AuthResponse mapToAuthResponse(EAccountCredentials user) {
    CustomUserDetails userDetails = CustomUserDetails.builder()
        .id(user.getId())
        .username(user.getStudentCode())
        .password(user.getPassword())
        .role(user.getRole())
        // .status(user.getIsActive())
        .build();
    TokenPair tokenPair = jwtUtils.createTokenPair(userDetails);
    return AuthResponse.builder()
    .refreshToken(tokenPair.refreshToken())
        .accessToken(tokenPair.accessToken())
        .build();
  }

  public void logout(String studentCode) {
    EAccountCredentials user = userRepository.findByStudentCode(studentCode)
        .orElseThrow(() -> new UsernameNotFoundException("Not found user by studentCode " + studentCode));
    user.setRefreshToken(null);
    userRepository.save(user);
  }

  public AuthResponse refreshToken(String refreshToken) {
    EAccountCredentials user = userRepository.findByRefreshToken(refreshToken)
        .orElseThrow(() -> new UsernameNotFoundException("Not found user by refresh token"));

    CustomUserDetails userDetails = CustomUserDetails.builder()
        .id(user.getId())
        .username(user.getStudentCode())
        .password(user.getPassword())
        .role(user.getRole())
        // .status(user.getIsActive())
        .build();

    TokenPair tokenPair = jwtUtils.createTokenPair(userDetails);
    user.setRefreshToken(tokenPair.refreshToken());
    userRepository.save(user);

    return AuthResponse.builder()
        .accessToken(tokenPair.accessToken())
        .refreshToken(refreshToken)
        .build();
  }
}
