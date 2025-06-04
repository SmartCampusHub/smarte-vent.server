package com.winnguyen1905.Activity.common.constant;

public enum ReportType {
  ACTIVITY("ACTIVITY"),
  USER("USER"),
  ORGANIZATION("ORGANIZATION");

  private final String value;

  ReportType(String value) {
    this.value = value;
  }
}
