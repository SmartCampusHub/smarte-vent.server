package com.winnguyen1905.Activity.common.constant;

public enum ActivityStatus {
  IN_PROGRESS("IN_PROGRESS"), COMPLETED("COMPLETED"), WAITING_TO_START("WAITING_TO_START"), CANCELLED("CANCELLED");

    private final String status;

    ActivityStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
} 
