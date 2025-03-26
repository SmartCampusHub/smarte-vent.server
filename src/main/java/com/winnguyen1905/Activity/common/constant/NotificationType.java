package com.winnguyen1905.Activity.common.constant;

public enum NotificationType {
    ACTIVITY("ACTIVITY"), LEARNING("LEARNING"), SECURITY("SECURITY");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
} 
