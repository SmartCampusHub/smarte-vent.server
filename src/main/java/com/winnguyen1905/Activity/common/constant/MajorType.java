package com.winnguyen1905.activity.common.constant;

public enum MajorType {
    IT("IT"),
    EE("EE"), // Electrical Engineering
    IS("IS"), // Information Security
    AE("AE"), // Automation Engineering
    AI("AI"); // Artificial Intelligence

    private final String type;

    MajorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
