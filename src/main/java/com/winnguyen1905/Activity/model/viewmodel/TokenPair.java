package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.*;

@Builder
public record TokenPair(
    String accessToken,
    String refreshToken) implements AbstractModel {
  @Builder
  public TokenPair(
      String accessToken,
      String refreshToken) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }
}
