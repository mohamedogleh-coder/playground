package com.hammi.playground.modules.fields;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FieldRequest(
        @NotNull(message = "Field capacity is required")
        Short capacity,
        @NotNull(message = "Field cost is required")
        BigDecimal cost,
        @NotNull(message = "Stop booking is required")
        Boolean stopBooking
) {
}
