package com.hammi.playground.modules.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventBookingRequest(
        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime startTime,
        Integer generatedCode
) {
}