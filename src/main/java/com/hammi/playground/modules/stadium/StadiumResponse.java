package com.hammi.playground.modules.stadium;

import java.util.UUID;

public record StadiumResponse(
        UUID stadiumId,
        String stadiumName,
        double latitude,
        double longitude,
        int extraTime,
        String profileUrl
) {
}
