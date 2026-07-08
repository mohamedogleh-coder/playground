package com.hammi.playground.modules.stadium;

import java.math.BigDecimal;

public record FilteredStadiumsResponse(
        String stadiumId,
        String stadiumName,
        int extraTime,
        Double distance,
        Integer fieldId,
        Integer capacity,
        BigDecimal fieldCost,
        Double longitude,
        Double latitude
) {
}
