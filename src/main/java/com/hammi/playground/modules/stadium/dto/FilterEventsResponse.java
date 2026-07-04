package com.hammi.playground.modules.stadium.dto;

public record FilterEventsResponse(
        String stadiumId,
        String stadiumName,
        Double distance,
        Integer capacity,
        Integer fieldId,
        Double longitude,
        Double latitude
) {
}
