package com.winnguyen1905.Activity.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken) {
}
