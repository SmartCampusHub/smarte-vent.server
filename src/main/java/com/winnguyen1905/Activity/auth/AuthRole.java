package com.winnguyen1905.Activity.auth;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRole {
  private String name;
  private String code;
  private List<AuthPermission> permissions;
  private List<String> permissionCodes;
}
