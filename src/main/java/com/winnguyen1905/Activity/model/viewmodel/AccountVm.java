package com.winnguyen1905.Activity.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.MajorType;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;
import lombok.Data;

public record AccountVm(
    Long id,
    String name,
    String email,
    String phone,
    String identifyCode,
    MajorType major,
    Boolean isActive,
    
    AccountRole role) implements AbstractModel {
  @Builder
  public AccountVm(
      Long id,
      String name,
      String email,
      String phone,
      String identifyCode,
      MajorType major,
      Boolean isActive,
      AccountRole role) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.identifyCode = identifyCode;
    this.major = major;
    this.isActive = isActive;
    this.role = role;
  }
}
