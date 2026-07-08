package com.hammi.playground.modules.working_days;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingDaysResponse(
        short id,
        DayOfWeek dayOfWeek,
        LocalTime openingTime,
        LocalTime closingTime,
        boolean isOpen
) {
}
