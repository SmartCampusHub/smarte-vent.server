package com.winnguyen1905.Activity.common.constant;

public enum ScheduleStatus {
  ONGOING("ONGOING"), FINISHED("FINISHED"), WAITING_TO_START("WAITING_TO_START");

  private final String status;

  ScheduleStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
