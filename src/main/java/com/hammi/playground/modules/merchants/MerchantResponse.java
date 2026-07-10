package com.hammi.playground.modules.merchants;

public record MerchantResponse(
        int merchantId,
        String merchantNumber,
        int providerId,
        String providerName,
        String providerService
) {
}
