package com.hammi.playground.modules.events.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

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
        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof EventPaymentMerchantRequest other)) return false;

                return Objects.equals(paymentMethod, other.paymentMethod)
                        && Objects.equals(merchantNumber, other.merchantNumber);
        }

        @Override
        public int hashCode() {
                return Objects.hash(paymentMethod, merchantNumber);
        }
}