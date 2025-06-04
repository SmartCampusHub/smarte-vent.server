package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.MajorType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AdminUpdateAccount(
    @NotBlank(message = "Full name is required") @Size(max = 100, message = "Full name must be less than 100 characters") String fullName,

    @NotBlank(message = "Email is required") @Email(message = "Email should be valid") @Size(max = 100, message = "Email must be less than 100 characters") String email,

    @NotBlank(message = "Phone number is required") @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits") String phone,

    @NotNull(message = "Major is required") MajorType major,

    @NotNull(message = "Role is required") AccountRole role,

    Boolean isActive) implements AbstractModel {
  @Builder
  public AdminUpdateAccount(
      String fullName,
      String email,
      String phone,
      MajorType major,
      AccountRole role,
      Boolean isActive) {
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
    this.major = major;
    this.role = role;
    this.isActive = isActive;
  }

}
