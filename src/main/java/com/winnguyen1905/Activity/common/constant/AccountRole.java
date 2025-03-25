package com.winnguyen1905.Activity.common.constant;

public enum AccountRole {
  ADMIN("ADMIN"), STUDENT("STUDENT"), GUESS("LECTURER");

  String role;

  AccountRole(String role) {
    this.role = role; 
  }
}
