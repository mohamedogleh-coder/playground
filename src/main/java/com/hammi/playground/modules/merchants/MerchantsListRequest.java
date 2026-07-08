package com.hammi.playground.modules.merchants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record MerchantsListRequest(@NotEmpty(message = "At least one merchant is required")
                                   Set<@Valid MerchantRequest> merchants) {


}
