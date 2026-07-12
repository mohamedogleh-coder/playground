package com.hammi.playground.modules.fields;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

public record FieldEventsResponse(
        short fieldId,
        short capacity,
        BigDecimal cost,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<TimeSlotsResponse> slots
) {
}


