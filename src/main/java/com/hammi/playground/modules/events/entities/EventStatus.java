package com.hammi.playground.modules.events.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventStatus {

    PENDING("pending"),
    CONFIRMED("confirmed"),
    COMPLETED("completed"),
    CANCELED("canceled");

    private final String value;

    EventStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventStatus fromValue(String value) {
        for (EventStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Unknown event status: " + value
        );
    }
}