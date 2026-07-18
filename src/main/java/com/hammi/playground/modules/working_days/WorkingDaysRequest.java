package com.hammi.playground.modules.working_days;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingDaysRequest(
//        @NotNull(message = "Week id is required")
        Short id,

        @NotNull(message = "Opening time is required")
        LocalTime openingTime,

        @NotNull(message = "Closing time is required")
        LocalTime closingTime,

        @NotNull(message = "Open is required")
        Boolean isOpen
) {
}