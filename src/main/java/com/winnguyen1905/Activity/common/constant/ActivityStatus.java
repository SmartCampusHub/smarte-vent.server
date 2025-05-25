package com.winnguyen1905.Activity.common.constant;

public enum ActivityStatus {
  IN_PROGRESS("IN_PROGRESS"), COMPLETED("COMPLETED"), PUBLISHED("PUBLISHED"), CANCELLED("CANCELLED"),
  CONFIRMED("CONFIRMED"), ONGOING("ONGOING"), PENDING("PENDING");

  private final String status;

  ActivityStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
