package com.hammi.playground.modules.stadium;

import java.util.UUID;

public record StadiumResponse(
        UUID stadiumId,
        String stadiumName,
        Double latitude,
        Double longitude,
        int extraTime,
        String profileUrl
) {
}
