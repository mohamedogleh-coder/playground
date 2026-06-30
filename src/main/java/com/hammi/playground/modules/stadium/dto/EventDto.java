package com.hammi.playground.modules.stadium.dto;

import java.time.LocalDateTime;

public record EventDto(int eventId, Integer eventKey, LocalDateTime startTime, LocalDateTime endTime) {
}
