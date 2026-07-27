package com.hammi.playground.modules.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record EventBookingRequest(
        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime startTime,

        String eventKey,

        UUID whoPaid,

        UUID receivedBy,
        @NotEmpty(message = "At least one merchant payment is required")
        @Valid
        Set<EventPaymentMerchantRequest> merchants,

        @NotBlank(message = "Payment status is required")
        @Pattern(
                regexp = "^(paid|partial|refunded)$",
                message = "Payment status must be one of: paid, partial, refunded"
        )
        String paymentStatus,

        @NotNull(message = "Discount is required")
        @DecimalMin(value = "0.00", message = "Discount cannot be negative")
        BigDecimal discounted
) {

    @AssertTrue(message = "Exactly one of whoPaid or receivedBy must be provided.")
    public boolean isValidPaymentParticipants() {
        return (whoPaid == null) != (receivedBy == null);
    }


    @AssertTrue(message = "Discount can only be applied by stadium manager.")
    public boolean isDiscountValid() {
        if (discounted == null || discounted.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return receivedBy != null;
    }
}

