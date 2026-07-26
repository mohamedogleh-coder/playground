package com.hammi.playground.modules.events.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EventPaymentMerchantRequest(
        @NotBlank(message = "Payment method is required")
        String paymentMethod,
        @NotBlank(message = "Merchant number is required")
        String merchantNumber,
        @NotNull(message = "Amount paid is required")
        @DecimalMin(value = "0.01", inclusive = true,
                message = "Amount paid must be greater than zero")
        BigDecimal amountPaid
) {

}