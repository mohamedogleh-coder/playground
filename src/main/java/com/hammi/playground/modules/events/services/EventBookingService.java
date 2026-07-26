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


        BigDecimal discount = request.discounted() == null
                ? BigDecimal.ZERO
                : request.discounted();


        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Discount cannot be negative");
        }


        if (discount.compareTo(totalAmount) > 0) {
            throw new ApiException(
                    "Discount cannot be greater than total amount"
            );
        }


        // Amount customer should pay after discount
        BigDecimal payableAmount = totalAmount.subtract(discount);


        BigDecimal amountPaid;


        if (request.paymentStatus().equals(PaymentStatus.PARTIAL.getValue())) {

            amountPaid = payableAmount.divide(
                    BigDecimal.valueOf(2),
                    2,
                    RoundingMode.HALF_UP
            );

        } else {

            amountPaid = payableAmount;
        }


        BigDecimal remaining = payableAmount.subtract(amountPaid);


        if (request.merchants() == null || request.merchants().isEmpty()) {
            throw new ApiException("At least one merchant payment is required");
        }


        BigDecimal merchantsTotal = request.merchants()
                .stream()
                .map(EventPaymentMerchantRequest::amountPaid)
                .peek(amount -> {
                    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ApiException(
                                "Merchant amount must be greater than zero"
                        );
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        if (merchantsTotal.compareTo(amountPaid) != 0) {
            throw new ApiException(
                    "Merchant payments total ($"
                            + merchantsTotal
                            + ") does not match required payment ($"
                            + amountPaid
                            + ")"
            );
        }


        var eventBooked = EventBooking.builder()
                .field(field)
                .paymentStatus(request.paymentStatus())
                .eventStatus(
                        request.paymentStatus()
                                .equals(PaymentStatus.PAID.getValue())
                                ?
                                EventStatus.CONFIRMED.getValue()
                                :
                                EventStatus.PENDING.getValue()
                )
                .remaining(remaining)
                .eventKey(request.generatedCode())
                .eventStart(request.startTime())
                .build();



        var bookingPayment = EventBookingPayment.builder()
                .event(eventBooked)
                .paidUser(request.whoPaid())
                .receivedBy(request.receivedBy())
                .discounted(discount)
                .build();



        var merchantPayments = request.merchants()
                .stream()
                .map(merchant ->
                        EventMerchantPayment.builder()
                                .payment(bookingPayment)
                                .merchantNumber(merchant.merchantNumber())
                                .paymentMethod(merchant.paymentMethod())
                                .amountPaid(merchant.amountPaid())
                                .build()
                )
                .toList();


        bookingPayment.setMerchantPayments(merchantPayments);


        eventBooked.getBookingPayments()
                .add(bookingPayment);


        var savedBooking = eventBookingRepository.save(eventBooked);


        return savedBooking.getId();
    }
//
//    @Transactional
//    public Integer bookEvent(Short fieldId, EventBookingRequest request) {
//
//        var field = fieldRepository.findById(fieldId)
//                .orElseThrow(() -> new NotFoundException("Field not exists"));
//
//
//        BigDecimal totalAmount = field.getCost()
//                .multiply(BigDecimal.valueOf(field.getCapacity()));
//
//
//        BigDecimal amountPaid = totalAmount;
//        BigDecimal remaining;
//
//
//
//        if (request.paymentStatus().equals(PaymentStatus.PARTIAL.getValue())) {
//
//            amountPaid = totalAmount.divide(
//                    BigDecimal.valueOf(2),
//                    2,
//                    RoundingMode.HALF_UP
//            );
//
//            remaining = totalAmount.subtract(amountPaid);
//        }
//
//        BigDecimal discount = request.discounted() == null
//                ? BigDecimal.ZERO
//                : request.discounted();
//
//
//        if (discount.compareTo(amountPaid) > 0) {
//            throw new ApiException(
//                    "Discount cannot be greater than amount paid"
//            );
//        }
//
//
//        BigDecimal finalAmountPaid = amountPaid.subtract(discount);
//
//        if (request.paymentStatus().equals(PaymentStatus.PAID.getValue())) {
//            remaining = BigDecimal.ZERO;
//        } else {
//            remaining = totalAmount.subtract(finalAmountPaid);
//        }
//        BigDecimal merchantsTotal = request.merchants()
//                .stream()
//                .map(EventPaymentMerchantRequest::amountPaid)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        if (merchantsTotal.compareTo(finalAmountPaid) != 0) {
//            throw new ApiException(
//                    "Merchant payments total ($" + merchantsTotal + ") does not match required amount ($" + finalAmountPaid + ")"
//            );
//        }
//
//        remaining = totalAmount.subtract(finalAmountPaid);
//        var eventBooked = EventBooking.builder()
//                .field(field)
//                .paymentStatus(request.paymentStatus())
//                .eventStatus(
//                        request.paymentStatus()
//                                .equals(PaymentStatus.PAID.getValue())
//                                ?
//                                EventStatus.CONFIRMED.getValue()
//                                :
//                                EventStatus.PENDING.getValue()
//                )
//                .remaining(remaining)
//                .eventKey(request.generatedCode())
//                .eventStart(request.startTime())
//                .build();
//
//        var bookingPayment = EventBookingPayment.builder()
//                .event(eventBooked)
//                .paidUser(request.whoPaid())
//                .receivedBy(request.receivedBy())
//                .discounted(discount)
//                .build();
//
//        var merchantPayments = request.merchants()
//                .stream()
//                .map(merchant -> EventMerchantPayment.builder()
//                        .payment(bookingPayment)
//                        .merchantNumber(merchant.merchantNumber())
//                        .paymentMethod(merchant.paymentMethod())
//                        .amountPaid(
//                                merchant.amountPaid()
//                        )
//                        .build()
//                )
//                .toList();
//
//
//        bookingPayment.setMerchantPayments(merchantPayments);
//
//        eventBooked.getBookingPayments()
//                .add(bookingPayment);
//
//        var savedBooking = eventBookingRepository.save(eventBooked);
//
//        return savedBooking.getId();
//    }
//
//    @Transactional
//    public Integer bookEvent(Short fieldId, EventBookingRequest request) {
//
//        var field = fieldRepository.findById(fieldId)
//                .orElseThrow(() -> new NotFoundException("Field not exists"));
//
//
//        BigDecimal totalAmount = field.getCost()
//                .multiply(BigDecimal.valueOf(field.getCapacity()));
//
//
//        BigDecimal paidAmount = request.merchants()
//                .stream()
//                .map(EventPaymentMerchantRequest::amountPaid)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//
//        BigDecimal discount = request.discounted() == null
//                ? BigDecimal.ZERO
//                : request.discounted();
//
//
//        if (discount.compareTo(paidAmount) > 0) {
//            throw new ApiException(
//                    "Discount cannot be greater than paid amount"
//            );
//        }
//
//
//        BigDecimal finalPaidAmount = paidAmount.subtract(discount);
//
//
//        BigDecimal remaining = totalAmount.subtract(finalPaidAmount);
//
//
//
//
//
//        var eventBooked = EventBooking.builder()
//                .field(field)
//                .paymentStatus(request.paymentStatus())
//                .eventStatus(
//                        request.paymentStatus()
//                                .equals(PaymentStatus.PAID.getValue())
//                                ?
//                                EventStatus.CONFIRMED.getValue()
//                                :
//                                EventStatus.PENDING.getValue()
//                )
//                .remaining(remaining)
//                .eventKey(request.generatedCode())
//                .eventStart(request.startTime())
//                .build();
//
//
//        var bookingPayment = EventBookingPayment.builder()
//                .event(eventBooked)
//                .paidUser(request.whoPaid())
//                .receivedBy(request.receivedBy())
//                .discounted(discount)
//                .build();
//
//
//        List<EventMerchantPayment> merchantPayments =
//                request.merchants()
//                        .stream()
//                        .map(item -> EventMerchantPayment.builder()
//                                .payment(bookingPayment)
//                                .merchantNumber(item.merchantNumber())
//                                .paymentMethod(item.paymentMethod())
//                                .amountPaid(item.amountPaid())
//                                .build()
//                        )
//                        .toList();
//
//
//        bookingPayment.getMerchantPayments().addAll(merchantPayments);
//
//
//        eventBooked.getBookingPayments()
//                .add(bookingPayment);
//
//
//        var savedBooked = eventBookingRepository.save(eventBooked);
//
//
//        return savedBooked.getId();
//    }

//    @Transactional
//    public Integer bookEvent(Short fieldId, EventBookingRequest request) {
//
//        var field = fieldRepository.findById(fieldId).orElseThrow(() -> new NotFoundException("Field not exists"));
//
//        BigDecimal totalAmount = field.getCost()
//                .multiply(BigDecimal.valueOf(field.getCapacity()));
//
//        BigDecimal amountPaid = totalAmount;
//        BigDecimal remaining = BigDecimal.ZERO;
//
//
//        if (request.paymentStatus().equals(PaymentStatus.PARTIAL.getValue())) {
//
//            amountPaid = totalAmount.divide(
//                    BigDecimal.valueOf(2),
//                    2,
//                    RoundingMode.HALF_UP
//            );
//
//            remaining = totalAmount.subtract(amountPaid);
//        }
//
//        BigDecimal discount = request.discounted() == null
//                ? BigDecimal.ZERO
//                : request.discounted();
//
//        if (discount.compareTo(amountPaid) > 0) {
//            throw new ApiException(
//                    "Discount amount (" + discount +
//                            ") cannot be greater than the payment amount (" + amountPaid + ")"
//            );
//        }
//
//        amountPaid = amountPaid.subtract(discount);
//
//        var eventBooked = EventBooking.builder()
//                .field(field)
//                .paymentStatus(request.paymentStatus())
//                .eventStatus(request.paymentStatus().equals(PaymentStatus.PAID.getValue())
//                        ? EventStatus.CONFIRMED.getValue() : EventStatus.PENDING.getValue())
//                .remaining(remaining)
//                .eventKey(request.generatedCode())
//                .eventStart(request.startTime())
//                .build();
//
//
//        var bookingPayment = EventBookingPayment.builder()
//                .event(eventBooked)
//                .paidUser(request.whoPaid())
//                .receivedBy(request.receivedBy())

    /// /                .paymentMethod(request.paymentMethod())
    /// /                .merchantNumber(request.merchantNumber())
    /// /                .amountPaid(amountPaid)
//                .discounted(discount)
//                .build();
//
//        eventBooked.getBookingPayments().add(bookingPayment);
//        var savedBooked = eventBookingRepository.save(eventBooked);
//        return savedBooked.getId();
//    }
    @Transactional
    public int bookAnotherHalf(Integer eventId, @Valid EventTakeHalfRequest request) {

        var event = eventBookingRepository.getEventWithEventPayments(eventId).orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getPaymentStatus().equals(PaymentStatus.PAID.getValue())) {
            throw new ApiException("This event fully paid");
        }

        if (event.getEventKey() != null) {
            if (request.eventKey() == null) {
                throw new ApiException("This event is private and requires a 4 digit event key");
            }
            if (!event.getEventKey().equals(request.eventKey())) {
                throw new ApiException("Invalid event key");
            }
        }

        BigDecimal remaining = event.getRemaining();
        BigDecimal amountPaid = remaining;


        if (request.discounted().compareTo(remaining) > 0) {
            throw new ApiException(
                    "Discount amount (" + request.discounted() +
                            ") cannot be greater than the remaining amount (" + remaining + ")"
            );
        }

        amountPaid = amountPaid.subtract(request.discounted());

        event.setRemaining(BigDecimal.ZERO);
        event.setEventStatus(EventStatus.CONFIRMED.getValue());
        event.setPaymentStatus(PaymentStatus.PAID.getValue());

        var bookingPayment = EventBookingPayment.builder().event(event).paidUser(request.payerId())
                .receivedBy(request.receivedById())
//                .paymentMethod(request.paymentMethod())
//                .merchantNumber(request.merchantNumber())
//                .amountPaid(amountPaid)
                .discounted(request.discounted()).build();

        event.getBookingPayments().add(bookingPayment);

        var updatedEvent = eventBookingRepository.save(event);
        return updatedEvent.getId();
    }

//    public EventInformationResponse getEventInformation(Integer eventId) {
//
//        var event = eventBookingRepository.getEventWithEventPayments(eventId)
//                .orElseThrow(() -> new NotFoundException("Event doesn't exist"));
//
//        var payments = event.getBookingPayments()
//                .stream()
//                .map(payment -> new EventPaymentResponse(
//                        payment.getMerchantNumber(),
//                        payment.getPaymentMethod(),
//                        payment.getAmountPaid(),
//                        payment.getDiscounted(),
//                        payment.getPaidAt()
//                ))
//                .toList();
//
//        return new EventInformationResponse(
//                event.getId(),
//                event.getEventStart(),
//                event.getEventEnd(),
//                event.getEventKey(),
//                event.getExtraTime(),
//                event.getPaymentStatus(),
//                event.getEventStatus(),
//                event.getRemaining(),
//                event.getDescription(),
//                payments
//        );
//    }

}
