package com.winnguyen1905.Activity.persistance.entity;

import com.winnguyen1905.Activity.common.constant.AccountRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "account")
public class EUserCredentials extends EBaseAudit {

  // @Column(name = "name")
  // private String name;

  @Column(name = "student_code") 
  private String studentCode;

  // @Column(name = "password")
  // private String password;

  // @Column(name = "status")
  // private Boolean isActive;

  // @Column(name = "email")
  // private String email;

  // @Column(name = "phone")
  // private String phone;

  // @Column(name = "refresh_token")
  // private String refreshToken;

  // @jakarta.persistence.Column(name = "role")
  // @Enumerated(EnumType.STRING)
  // private AccountRole role;
}
