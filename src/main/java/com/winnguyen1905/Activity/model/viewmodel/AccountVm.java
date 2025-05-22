package com.winnguyen1905.Activity.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;
import lombok.Data;

public record AccountVm(
    Long id,
    String name,
    String email,
    String phone,
    String studentCode,
    Boolean isActive,
    AccountRole role) implements AbstractModel {
  @Builder
  public AccountVm(
      Long id,
      String name,
      String email,
      String phone,
      String studentCode,
      Boolean isActive,
      AccountRole role) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.studentCode = studentCode;
    this.isActive = isActive;
    this.role = role;
  }
}
