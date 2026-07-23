package com.hammi.playground.modules.events;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
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

        var field = fieldRepository.findById(fieldId).orElseThrow(() -> new NotFoundException("Field not exists"));

        BigDecimal totalAmount = field.getCost()
                .multiply(BigDecimal.valueOf(field.getCapacity()));

        BigDecimal amountPaid = totalAmount;
        BigDecimal remaining = BigDecimal.ZERO;


        if (request.paymentStatus().equals(PaymentStatus.PARTIAL.getValue())) {

            amountPaid = totalAmount.divide(
                    BigDecimal.valueOf(2),
                    2,
                    RoundingMode.HALF_UP
            );

            remaining = totalAmount.subtract(amountPaid);
        }

        BigDecimal discount = request.discounted() == null
                ? BigDecimal.ZERO
                : request.discounted();

        if (discount.compareTo(amountPaid) > 0) {
            throw new ApiException(
                    "Discount amount (" + discount +
                            ") cannot be greater than the payment amount (" + amountPaid + ")"
            );
        }

        amountPaid = amountPaid.subtract(discount);

        var eventBooked = EventBooking.builder()
                .field(field)
                .paymentStatus(request.paymentStatus())
                .eventStatus(request.paymentStatus().equals(PaymentStatus.PAID.getValue())
                        ? EventStatus.CONFIRMED.getValue() : EventStatus.PENDING.getValue())
                .remaining(remaining)
                .eventKey(request.generatedCode())
                .eventStart(request.startTime())
                .build();


        var bookingPayment = EventBookingPayment.builder()
                .event(eventBooked)
                .paidUser(request.whoPaid())
                .receivedBy(request.receivedBy())
                .paymentMethod(request.paymentMethod())
                .merchantNumber(request.merchantNumber())
                .amountPaid(amountPaid)
                .discounted(discount)
                .build();

        eventBooked.getBookingPayments().add(bookingPayment);
        var savedBooked = eventBookingRepository.save(eventBooked);
        return savedBooked.getId();
    }

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
                .receivedBy(request.receivedById()).paymentMethod(request.paymentMethod())
                .merchantNumber(request.merchantNumber())
                .amountPaid(amountPaid).discounted(request.discounted()).build();

        event.getBookingPayments().add(bookingPayment);

        var updatedEvent = eventBookingRepository.save(event);
        return updatedEvent.getId();
    }

    public EventInformationResponse getEventInformation(Integer eventId) {

        var event = eventBookingRepository.getEventWithEventPayments(eventId)
                .orElseThrow(() -> new NotFoundException("Event doesn't exist"));

        var payments = event.getBookingPayments()
                .stream()
                .map(payment -> new EventPaymentResponse(
                        payment.getMerchantNumber(),
                        payment.getPaymentMethod(),
                        payment.getAmountPaid(),
                        payment.getDiscounted(),
                        payment.getPaidAt()
                ))
                .toList();

        return new EventInformationResponse(
                event.getId(),
                event.getEventStart(),
                event.getEventEnd(),
                event.getEventKey(),
                event.getExtraTime(),
                event.getPaymentStatus(),
                event.getEventStatus(),
                event.getRemaining(),
                event.getDescription(),
                payments
        );
    }

}
