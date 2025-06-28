package com.winnguyen1905.activity.common.constant;

public enum ScheduleStatus {
  IN_PROGRESS("IN_PROGRESS"), COMPLETED("COMPLETED"), WAITING_TO_START("WAITING_TO_START"), CANCELLED("CANCELLED");

  private final String status;

  ScheduleStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
