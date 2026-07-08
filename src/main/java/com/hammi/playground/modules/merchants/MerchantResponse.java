package com.hammi.playground.modules.merchants;

public record MerchantResponse(
        int merchantId,
        String merchantNumber,
        String providerName,
        String providerService
) {
}
