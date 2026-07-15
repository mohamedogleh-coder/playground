package com.hammi.playground.modules.events;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.fields.Field;
import com.hammi.playground.modules.fields.FieldRepository;
import com.hammi.playground.modules.fields.TimeSlotsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    final EventBookingRepository eventBookingRepository;
    final FieldRepository fieldRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;


    public List<TimeSlotsResponse> generateEventSlots(Short fieldId, LocalDate date) {

        return jdbcTemplate.queryForObject(
                """
                        SELECT get_field_events_fn(
                            CAST(? AS date),
                            CAST(? AS smallint)
                        ) AS slots
                        """,
                (rs, _) -> {
                    String slotsJson = rs.getString("slots");

                    return objectMapper.readValue(
                            slotsJson,
                            new TypeReference<>() {
                            }
                    );
                },
                date,
                fieldId
        );
    }

    public int takeEvent(Short fieldId, EventBookingRequest request) {
        var field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new NotFoundException("Field not found"));

        BigDecimal totalAmount = field.getCost()
                .multiply(BigDecimal.valueOf(field.getCapacity()));

        BigDecimal amountPaid = totalAmount;
        BigDecimal remaining = BigDecimal.ZERO;

        if (request.eventStatus() == EventStatus.HALF) {

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


        var event = EventBookings.builder()
                .field(field)
                .eventStatus(request.eventStatus().getValue())
                .remaining(remaining)
                .eventKey(request.generatedCode())
                .eventStart(request.startTime())
                .build();


        var bookingPayment = EventBookingPayment.builder()
                .event(event)
                .payerId(request.payerId())
                .receivedById(request.receivedById())
                .paymentMethod(request.paymentMethod())
                .merchantNumber(request.merchantNumber())
                .amountPaid(amountPaid)
                .discountAmount(discount)
                .build();

        event.getBookingPayments().add(bookingPayment);

        var savedEvent = eventBookingRepository.save(event);

        return savedEvent.getId();
    }

    public int takeAnotherHalf(Integer eventId, @Valid EventTakeHalfRequest request) {

        var event = eventBookingRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        if (event.getEventStatus().equals(EventStatus.FULL.getValue())) {
            throw new ApiException("This event fully taken");
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

        if (request.discounted() == null && remaining.compareTo(request.amountPaid()) != 0) {
            throw new ApiException(
                    "Remaining amount must be " + remaining
            );
        }

        event.setRemaining(BigDecimal.ZERO);
        event.setEventStatus(EventStatus.FULL.getValue());

        var bookingPayment = EventBookingPayment.builder().event(event).payerId(request.payerId())
                .receivedById(request.receivedById()).paymentMethod(request.paymentMethod())
                .merchantNumber(request.merchantNumber())
                .amountPaid(request.amountPaid()).discountAmount(request.discounted()).build();

        event.getBookingPayments().add(bookingPayment);

        var updatedEvent = eventBookingRepository.save(event);
        return updatedEvent.getId();
    }

    public EventInformationResponse getEventInformation(Integer eventId) {

        var event = eventBookingRepository.getEventWithEventPayments(eventId)
                .orElseThrow(() -> new NotFoundException("Event does not exist"));

        var payments = event.getBookingPayments()
                .stream()
                .map(payment -> new EventPayments(
                        payment.getMerchantNumber(),
                        payment.getPaymentMethod(),
                        payment.getAmountPaid(),
                        payment.getDiscountAmount(),
                        payment.getPaidAt()
                ))
                .toList();

        return new EventInformationResponse(
                event.getId(),
                event.getEventStart(),
                event.getEventEnd(),
                event.getEventKey(),
                event.getExtraTime(),
                event.getEventStatus(),
                event.getRemaining(),
                payments
        );
    }


}
