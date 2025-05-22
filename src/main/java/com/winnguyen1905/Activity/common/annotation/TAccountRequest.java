package com.winnguyen1905.Activity.common.annotation;

import java.io.Serializable;
import java.util.UUID;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

public record TAccountRequest(
    Long id,
    String username,
    AccountRole role,
    UUID socketClientId) implements Serializable {
  @Builder
  public TAccountRequest(
      Long id,
      String username,
      AccountRole role,
      UUID socketClientId) {
    this.id = id;
    this.username = username;
    this.role = role;
    this.socketClientId = socketClientId;
  }
}
