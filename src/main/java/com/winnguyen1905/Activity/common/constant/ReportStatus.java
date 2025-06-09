package com.winnguyen1905.Activity.common.constant;

public enum ReportStatus {
  SPENDING("SPENDING"),
  REJECTED("REJECTED"),
  ACTION_TAKEN("ACTION_TAKEN");

  private final String status;

  ReportStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }
}
