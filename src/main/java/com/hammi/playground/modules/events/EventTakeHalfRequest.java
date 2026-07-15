package com.hammi.playground.modules.events;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record EventTakeHalfRequest(
        Integer eventKey,

        UUID payerId,

        UUID receivedById,

        @NotNull(message = "Payment method is required")
        String paymentMethod,

        String merchantNumber,

        BigDecimal discounted,

        @NotNull(message = "Amount paid amount is required")
        BigDecimal amountPaid
) {

    @AssertTrue(message = "Exactly one of payerId or receivedById must be provided.")
    public boolean isValidPaymentParticipants() {
        return (payerId == null) != (receivedById == null);
    }

    @AssertTrue(message = "merchantNumber number is required for non-cash payments.")
    public boolean isValidMerchantNumber() {
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            return merchantNumber == null || merchantNumber.isBlank();
        }
        return merchantNumber != null && !merchantNumber.isBlank();
    }


    @AssertTrue(message = "discounted can only be applied by stadium manager.")
    public boolean isDiscountValid() {
        if (discounted == null || discounted.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return receivedById != null;
    }
}
