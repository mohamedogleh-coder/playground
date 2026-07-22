package com.hammi.playground.modules.stadium;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record StadiumRegRequest(
        @NotBlank(message = "Stadium name is required") @Size(max = 50, message = "Stadium name must not exceed 50 characters") String stadiumName,

        @NotNull(message = "Manager id is required") UUID managerId,

        @NotNull(message = "Extra time is required") @Min(value = 0, message = "Extra time cannot be negative") @Max(value = 8, message = "Extra time cannot exceed 8 minutes")

        Short extraTime,
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90") @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") Double latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180") @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") Double longitude,

        @NotNull(message = "half booking is required")
        Boolean halfBooking,

        String profileUrl) {
}
