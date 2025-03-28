package com.winnguyen1905.Activity.config;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winnguyen1905.Activity.model.viewmodel.RestResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component("authenticationEntryPoint")
public class CustomServerAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
            AuthenticationException authException) throws IOException, ServletException {
        
        delegate.commence(request, response, authException);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        RestResponse<Object> res = RestResponse.builder()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .message("Authentication failed, please check your token")
            .error(Optional.ofNullable(authException.getCause())
                .map(Throwable::getMessage)
                .orElse(authException.getMessage()))
            .build();
        objectMapper.writeValue(response.getOutputStream(), res);
    }
}
