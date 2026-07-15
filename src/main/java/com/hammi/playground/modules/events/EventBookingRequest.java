package com.hammi.playground.modules.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventBookingRequest(
        @NotNull(message = "Start time is required")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime startTime,

        Integer generatedCode,

        UUID payerId,

        UUID receivedById,

        @NotNull(message = "Payment method is required")
        String paymentMethod,

        String merchantNumber,

        @NotNull(message = "Event type is required")
        EventStatus eventStatus,

        @NotNull(message = "Discount is required")
        BigDecimal discounted
) {

    @AssertTrue(message = "Exactly one of payerId or receivedById must be provided.")
    public boolean isValidPaymentParticipants() {
        return (payerId == null) != (receivedById == null);
    }

    @AssertTrue(message = "Merchant number is required for non-cash payments.")
    public boolean isValidMerchantNumber() {
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            return merchantNumber == null || merchantNumber.isBlank();
        }
        return merchantNumber != null && !merchantNumber.isBlank();
    }

//    @AssertTrue(message = "Generated code is required when event type is HALF.")
//    public boolean isGeneratedCodeValid() {
//        return eventStatus != EventStatus.HALF || generatedCode != null;
//    }

    @AssertTrue(message = "Discount can only be applied by stadium manager.")
    public boolean isDiscountValid() {
        if (discounted == null || discounted.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return receivedById != null;
    }
}