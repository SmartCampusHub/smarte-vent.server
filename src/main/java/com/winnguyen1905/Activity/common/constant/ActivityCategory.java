package com.winnguyen1905.activity.common.constant;

public enum ActivityCategory {
  STUDENT_ORGANIZATION("STUDENT_ORGANIZATION"), UNIVERSITY("UNIVERSITY"), THIRD_PARTY("THIRD_PARTY");

  private final String value;

  ActivityCategory(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
