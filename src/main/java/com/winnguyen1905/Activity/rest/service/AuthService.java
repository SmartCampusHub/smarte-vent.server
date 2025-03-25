package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.model.dto.LoginRequest;
import com.winnguyen1905.Activity.model.dto.RegisterRequest;
import com.winnguyen1905.Activity.model.viewmodel.AuthResponse;

public interface AuthService {
    void logout(String username);
    AuthResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    AuthResponse handleRefreshToken(String refreshToken);
}
