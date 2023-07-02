package org.ocp;

public enum NotificationEmergencyLevel {

    URGENCY(0), SEVERE(1);

    private final int value;
    NotificationEmergencyLevel(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}
