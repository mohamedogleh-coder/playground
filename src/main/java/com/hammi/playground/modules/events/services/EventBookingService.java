package com.hammi.playground.modules.events.services;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.events.*;
import com.hammi.playground.modules.events.dto.EventBookingRequest;
import com.hammi.playground.modules.events.dto.EventPaymentMerchantRequest;
import com.hammi.playground.modules.events.dto.EventTakeHalfRequest;
import com.hammi.playground.modules.events.entities.EventBooking;
import com.hammi.playground.modules.events.entities.EventBookingPayment;
import com.hammi.playground.modules.events.entities.EventStatus;
import com.hammi.playground.modules.events.entities.PaymentStatus;
import com.hammi.playground.modules.events.repos.EventBookingRepository;
import com.hammi.playground.modules.fields.FieldRepository;
import com.hammi.playground.modules.fields.TimeSlotsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventBookingService {
    private final EventBookingRepository eventBookingRepository;
    private final FieldRepository fieldRepository;
    private final ObjectMapper objectMapper;

    public List<TimeSlotsResponse> generateEventSlots(Short fieldId, LocalDate date) {
        String json = eventBookingRepository.getTimeSlots(fieldId, date);

        return objectMapper.readValue(
                json,
                new TypeReference<>() {
                }
        );
    }

    @Transactional
    public Integer bookEvent(Short fieldId, EventBookingRequest request) {

        var field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new NotFoundException("Field not exists"));

        BigDecimal totalAmount = field.getCost()
                .multiply(BigDecimal.valueOf(field.getCapacity()));

        BigDecimal discount = validateDiscount(
                request.discounted(),
                totalAmount
        );

        BigDecimal payableAmount = totalAmount.subtract(discount);

        BigDecimal amountPaid = request.paymentStatus().equals(PaymentStatus.PARTIAL.getValue())
                ? payableAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                : payableAmount;

        BigDecimal remaining = payableAmount.subtract(amountPaid);

        validateMerchantPayments(request.merchants(), amountPaid);

        var eventBooked = EventBooking.builder()
                .field(field)
                .eventKey(request.eventKey())
                .paymentStatus(request.paymentStatus())
                .eventStatus(getEventStatus(request.paymentStatus()))
                .remaining(remaining)
                .eventStart(request.startTime())
                .build();

        eventBooked.getBookingPayments().add(
                buildBookingPayment(
                        eventBooked,
                        request.whoPaid(),
                        request.receivedBy(),
                        discount,
                        request.merchants()
                )
        );

        return eventBookingRepository.save(eventBooked).getId();
    }

    @Transactional
    public Integer bookAnotherHalf(Integer eventId,
                                   @Valid EventTakeHalfRequest request) {

        var event = eventBookingRepository.getEventWithEventPayments(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (PaymentStatus.PAID.getValue().equals(event.getPaymentStatus())) {
            throw new ApiException("This event is already fully paid");
        }

        if (event.getEventKey() != null) {
            if (request.eventKey() == null) {
                throw new ApiException("This event is private and requires a 4 digit event key");
            }

            if (!event.getEventKey().equals(request.eventKey())) {
                throw new ApiException("Invalid event key");
            }
        }

        BigDecimal discount = validateDiscount(
                request.discounted(),
                event.getRemaining()
        );

        BigDecimal amountPaid = event.getRemaining().subtract(discount);

        validateMerchantPayments(request.merchants(), amountPaid);

        event.setRemaining(BigDecimal.ZERO);
        event.setPaymentStatus(PaymentStatus.PAID.getValue());
        event.setEventStatus(EventStatus.CONFIRMED.getValue());

        event.getBookingPayments().add(
                buildBookingPayment(
                        event,
                        request.payerId(),
                        request.receivedById(),
                        discount,
                        request.merchants()
                )
        );

        return eventBookingRepository.save(event).getId();
    }

    private EventBookingPayment buildBookingPayment(
            EventBooking event,
            UUID paidUser,
            UUID receivedBy,
            BigDecimal discount,
            Set<EventPaymentMerchantRequest> merchants
    ) {

        var bookingPayment = EventBookingPayment.builder()
                .event(event)
                .paidUser(paidUser)
                .receivedBy(receivedBy)
                .discounted(discount)
                .build();

        var merchantPayments = merchants.stream()
                .map(merchant -> EventMerchantPayment.builder()
                        .payment(bookingPayment)
                        .merchantNumber(merchant.merchantNumber())
                        .paymentMethod(merchant.paymentMethod())
                        .amountPaid(merchant.amountPaid())
                        .build())
                .toList();

        bookingPayment.setMerchantPayments(merchantPayments);

        return bookingPayment;
    }


    private String getEventStatus(String paymentStatus) {
        return PaymentStatus.PAID.getValue().equals(paymentStatus)
                ? EventStatus.CONFIRMED.getValue()
                : EventStatus.PENDING.getValue();
    }

    private BigDecimal validateDiscount(BigDecimal discount, BigDecimal maxAmount) {

        BigDecimal actualDiscount =
                discount == null ? BigDecimal.ZERO : discount;

        if (actualDiscount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Discount cannot be negative");
        }

        if (actualDiscount.compareTo(maxAmount) > 0) {
            throw new ApiException(
                    "Discount cannot be greater than amount (" + maxAmount + ")"
            );
        }

        return actualDiscount;
    }

    private void validateMerchantPayments(Set<EventPaymentMerchantRequest> merchants, BigDecimal expectedAmount) {

        if (merchants == null || merchants.isEmpty()) {
            throw new ApiException("At least one merchant payment is required");
        }

        BigDecimal merchantsTotal = merchants.stream()
                .map(EventPaymentMerchantRequest::amountPaid)
                .peek(amount -> {
                    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ApiException(
                                "Merchant amount must be greater than zero"
                        );
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (merchantsTotal.compareTo(expectedAmount) != 0) {
            throw new ApiException(
                    "Merchant payments total ($" + merchantsTotal +
                            ") does not match required payment ($" + expectedAmount + ")"
            );
        }
    }
}
