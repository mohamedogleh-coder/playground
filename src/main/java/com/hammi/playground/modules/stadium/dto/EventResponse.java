package com.hammi.playground.modules.stadium.dto;

import java.time.LocalDateTime;

public record EventResponse(
        LocalDateTime startTime,
        LocalDateTime endTime,
        boolean isAvailable,
        Integer eventId
) {
}
