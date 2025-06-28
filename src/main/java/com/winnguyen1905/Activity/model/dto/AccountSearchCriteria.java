package com.winnguyen1905.activity.model.dto;

import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.MajorType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchCriteria implements AbstractModel {
  private String fullName;
  private String email;
  private String phone;
  private String identifyCode;
  private AccountRole role;
  private MajorType major;
  private Boolean isActive;
}
