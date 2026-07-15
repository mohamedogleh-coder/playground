package com.hammi.playground.modules.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventInformationResponse(
        int eventId,
        LocalDateTime eventStart,
        LocalDateTime eventEnd,
        Integer eventKey,
        Short extraTime,
        String eventStatus,
        BigDecimal remaining,
        List<EventPayments> payments
) {
}

record EventPayments(
        String merchantNumber,
        String paymentMethod,
        BigDecimal amountPaid,
        BigDecimal discounted,
        LocalDateTime paidAt
) {
}