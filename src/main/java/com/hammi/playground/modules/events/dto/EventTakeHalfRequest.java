package com.hammi.playground.modules.events.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record EventTakeHalfRequest(
        String eventKey,

        UUID payerId,

        UUID receivedById,

        @NotEmpty(message = "At least one merchant payment is required")
        @Valid
        Set<EventPaymentMerchantRequest> merchants,

        @NotNull(message = "Discount cant be null")
        BigDecimal discounted
) {

    @AssertTrue(message = "Exactly one of payerId or receivedById must be provided.")
    public boolean isValidPaymentParticipants() {
        return (payerId == null) != (receivedById == null);
    }


    @AssertTrue(message = "discounted can only be applied by stadium manager.")
    public boolean isDiscountValid() {
        if (discounted == null || discounted.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        return receivedById != null;
    }
}
