package com.winnguyen1905.activity.common.constant;

public enum ActivityStatus {
  IN_PROGRESS("IN_PROGRESS"), COMPLETED("COMPLETED"), PUBLISHED("PUBLISHED"), CANCELLED("CANCELLED"), PENDING("PENDING");

  private final String status;

  ActivityStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
