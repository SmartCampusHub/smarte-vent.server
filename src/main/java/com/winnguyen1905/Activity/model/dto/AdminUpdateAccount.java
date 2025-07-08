package com.winnguyen1905.activity.model.dto;

import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.MajorType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateAccount implements AbstractModel {
  private String fullName;

  @Email(message = "Email format invalid")
  private String email;

  private String phone;
  private MajorType major;
  private AccountRole role;
  private Boolean isActive;
}
