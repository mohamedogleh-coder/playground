package com.hammi.playground.modules.working_days;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record WorkingDaysRequestList(
        @NotEmpty(message = "At least one working day is required")
        Set<@Valid WorkingDaysRequest> workingDays
) {
}