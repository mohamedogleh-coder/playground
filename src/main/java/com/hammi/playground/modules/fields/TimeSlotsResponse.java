package com.hammi.playground.modules.fields;


record TimeSlotsResponse(
        String startTime,
        String endTime,
        Short eventId,
        Integer eventKey,
        String eventStatus
) {
}
