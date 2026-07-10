package com.hammi.playground.modules.working_days;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingDaysResponse(
        short id,
        DayOfWeek dayOfWeek,
        @JsonFormat(pattern = "HH:mm")
        LocalTime openingTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime closingTime,
        boolean isOpen
) {
}
