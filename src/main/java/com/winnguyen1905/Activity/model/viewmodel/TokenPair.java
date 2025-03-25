package com.winnguyen1905.Activity.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Builder
public record TokenPair(
    String tokenType,
    Integer expiresIn,
    String accessToken,
    String refreshToken) {
}
