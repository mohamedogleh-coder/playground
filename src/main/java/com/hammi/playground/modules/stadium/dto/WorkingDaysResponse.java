package com.hammi.playground.modules.stadium.dto;

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
