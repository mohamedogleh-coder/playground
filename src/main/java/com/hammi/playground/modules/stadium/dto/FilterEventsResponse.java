package com.hammi.playground.modules.stadium.dto;

import java.math.BigDecimal;

public record FilterEventsResponse(
        String stadiumId,
        String stadiumName,
        Double distance,
        Integer fieldId,
        Integer capacity,
        BigDecimal fieldCost,
        Double longitude,
        Double latitude
) {
}
