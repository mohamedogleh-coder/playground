package com.hammi.playground.modules.events;


public record EventsBookedSummery(
        short fieldId,
        short capacity,
        short pendingEvents,
        short confirmedEvents,
        short completedEvents,
        short canceledEvents
) {
}
