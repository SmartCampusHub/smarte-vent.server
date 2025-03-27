package com.winnguyen1905.Activity.common.constant;

public enum ActivityCategory {
  CLUB("CLUB"), SCHOOL("SCHOOL"), THIRD_PARTY("THIRD_PARTY");

  private final String value;

  ActivityCategory(String value) {
      this.value = value;
  }

  public String getValue() {
    return value;
  }
}
