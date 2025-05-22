package com.winnguyen1905.Activity.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

public record AuthResponse(String accessToken, AccountVm account, @JsonIgnore String refreshToken)
    implements AbstractModel {
      
  @Builder
  public AuthResponse(
      String accessToken,
      AccountVm account,
      String refreshToken) {
    this.accessToken = accessToken;
    this.account = account;
    this.refreshToken = refreshToken;
  }

}
