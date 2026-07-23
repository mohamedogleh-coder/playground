package com.hammi.playground.modules.events;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record EventTakeHalfRequest(
        String eventKey,

        UUID payerId,

        UUID receivedById,

        String paymentMethod,

        @NotNull(message = "Merchant number is required")
        String merchantNumber,

        @NotNull(message = "Discount cant be null")
        BigDecimal discounted
) {

    @AssertTrue(message = "Exactly one of payerId or receivedById must be provided.")
    public boolean isValidPaymentParticipants() {
        return (payerId == null) != (receivedById == null);
    }


    @AssertTrue(message = "Payment method and merchant number must be provided together.")
    public boolean isValidPaymentMethod() {

        boolean methodProvided = paymentMethod != null && !paymentMethod.isBlank();
        boolean merchantProvided = merchantNumber != null && !merchantNumber.isBlank();

        return methodProvided == merchantProvided;
    }


    @AssertTrue(message = "discounted can only be applied by stadium manager.")
    public boolean isDiscountValid() {
        if (discounted == null || discounted.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return receivedById != null;
    }
}
