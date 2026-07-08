package com.hammi.playground.modules.merchants;

import jakarta.validation.constraints.NotNull;

public record MerchantRequest(
        @NotNull(message = "Merchant number is required")
        String merchantNumber,
        @NotNull(message = "Provider id is required")
        Short providerId
) {
}
