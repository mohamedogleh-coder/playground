package com.hammi.playground.modules.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventBookingRequest(
        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime startTime,

        String generatedCode,

        UUID whoPaid,

        UUID receivedBy,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        String merchantNumber,

        @NotBlank(message = "Payment status is required")
        @Pattern(
                regexp = "^(paid|partial|refunded)$",
                message = "Payment status must be one of: paid, partial, refunded"
        )
        String paymentStatus,


        @NotNull(message = "Discount is required")
        BigDecimal discounted
) {

    @AssertTrue(message = "Exactly one of whoPaid or receivedBy must be provided.")
    public boolean isValidPaymentParticipants() {
        return (whoPaid == null) != (receivedBy == null);
    }

    @AssertTrue(message = "Merchant number is required for non-cash payments.")
    public boolean isValidMerchantNumber() {
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            return merchantNumber == null || merchantNumber.isBlank();
        }
        return merchantNumber != null && !merchantNumber.isBlank();
    }


    @AssertTrue(message = "Discount can only be applied by stadium manager.")
    public boolean isDiscountValid() {
        if (discounted == null || discounted.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return receivedBy != null;
    }
}