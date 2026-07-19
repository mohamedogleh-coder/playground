package com.hammi.playground.modules.events;


public record EventsBookedSummery(
        short fieldId,
        short capacity,
        String date,
        short totalEvents) {
}
