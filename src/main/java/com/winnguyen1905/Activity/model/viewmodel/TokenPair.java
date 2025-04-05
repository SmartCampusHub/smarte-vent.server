package com.winnguyen1905.Activity.model.viewmodel;

import lombok.*;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken) {
}
