package com.hammi.playground.modules.fields;

import java.math.BigDecimal;

public record FieldResponse(
        short id,
        short capacity,
        BigDecimal cost
) {

}
