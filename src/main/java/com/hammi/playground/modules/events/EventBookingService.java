package com.hammi.playground.modules.events;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.fields.FieldRepository;
import com.hammi.playground.modules.fields.TimeSlotsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventBookingService {
    private final EventBookingRepository eventBookingRepository;
    private final FieldRepository fieldRepository;
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

                    return objectMapper.readValue(slotsJson, new TypeReference<>() {
                            }
                    );
                },
                date,
                fieldId
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

//    public List<EventsBookedSummery> getEventBookingStatusSummaryByDateRange(
//            UUID stadiumId,
//            LocalDate startDate,
//            LocalDate endDate
//    ) {
//
//        String query = """
//                SELECT
//                    f.id AS field_id,
//                    f.capacity,
//
//                    COUNT(eb.id) FILTER (
//                        WHERE eb.event_status = 'pending'
//                    ) AS pending_events,
//
//                    COUNT(eb.id) FILTER (
//                        WHERE eb.event_status = 'confirmed'
//                    ) AS confirmed_events,
//
//                    COUNT(eb.id) FILTER (
//                        WHERE eb.event_status = 'completed'
//                    ) AS completed_events,
//
//                    COUNT(eb.id) FILTER (
//                        WHERE eb.event_status = 'canceled'
//                    ) AS canceled_events
//
//                FROM public.fields f
//
//                LEFT JOIN public.event_bookings eb
//                    ON f.id = eb.field_id
//                    AND eb.event_start >= ?
//                    AND eb.event_start < ? + INTERVAL '1 day'
//
//                WHERE f.stadium_id = ?
//
//                GROUP BY
//                    f.id,
//                    f.capacity;
//                """;
//
//
//        return jdbcTemplate.query(
//                query,
//                (rs, _) -> new EventsBookedSummery(
//                        rs.getShort("field_id"),
//                        rs.getShort("capacity"),
//                        rs.getString("event_start"),
//                        rs.getShort("pending_events"),
//                        rs.getShort("confirmed_events"),
//                        rs.getShort("completed_events"),
//                        rs.getShort("canceled_events")
//                ),
//                startDate,
//                endDate,
//                stadiumId
//        );
//    }
////
//    public List<EventsBookedSummery> getEventsBookingSpecificDate(UUID stadiumId, LocalDate startDate, LocalDate endDate) {
//        String query = """
//                 SELECT
//                     f.id AS field_id,
//                     f.capacity,
//                     COUNT(*) FILTER (WHERE eb.event_status = 'pending')   AS pending_events,
//                     COUNT(*) FILTER (WHERE eb.event_status = 'confirmed') AS confirmed_events,
//                     COUNT(*) FILTER (WHERE eb.event_status = 'completed') AS completed_events,
//                     COUNT(*) FILTER (WHERE eb.event_status = 'canceled')  AS canceled_events
//                 FROM public.fields f
//                 LEFT JOIN public.event_bookings eb
//                     ON f.id = eb.field_id
//                     AND eb.event_start::date BETWEEN ? AND ?
//                 WHERE f.stadium_id = ?
//                 GROUP BY
//                     f.id,
//                     f.capacity;
//                """;
//
//        return jdbcTemplate.query(query, (rs, _) -> new EventsBookedSummery(
//                rs.getShort("field_id"),
//                rs.getShort("capacity"),
//                rs.getString("event_start"),
//                rs.getShort("pending_events"),
//                rs.getShort("confirmed_events"),
//                rs.getShort("completed_events"),
//                rs.getShort("canceled_events")
//        ), startDate, endDate, stadiumId);
//    }


}
