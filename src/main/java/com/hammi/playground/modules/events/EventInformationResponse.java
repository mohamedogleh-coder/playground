package com.hammi.playground.modules.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventInformationResponse(
        int eventId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventStart,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventEnd,
        String eventKey,
        Short extraTime,
        String eventStatus,
        String paymentStatus,
        BigDecimal remaining,
        String description,
        List<EventPaymentResponse> payments
) {
}

record EventPaymentResponse(
        String merchantNumber,
        String paymentMethod,
        BigDecimal amountPaid,
        BigDecimal discounted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime paidAt
) {
}