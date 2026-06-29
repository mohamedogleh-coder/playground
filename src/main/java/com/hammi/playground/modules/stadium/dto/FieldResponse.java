package com.hammi.playground.modules.stadium.dto;

import lombok.Data;

import java.math.BigDecimal;

public record FieldResponse(
        short id,
        short capacity,
        BigDecimal cost
) {

}
