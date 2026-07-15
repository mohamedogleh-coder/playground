package com.hammi.playground.modules.fields;

import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.events.*;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FieldsService {
    private final FieldRepository fieldRepository;
    private final ObjectMapper objectMapper;
    private final StadiumRepository stadiumRepository;
    private final EventBookingRepository eventBookingRepository;
    private final JdbcTemplate jdbcTemplate;


    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
        return stadium.getFields().stream().map((field -> new FieldResponse(field.getId(), field.getCapacity(), field.getCost()))).toList();
    }

    public FieldResponse addField(UUID stadiumId, FieldRequest request) {
        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));

        var newField = Field.builder().cost(request.cost()).capacity(request.capacity()).stadium(stadium).build();
        var savedField = fieldRepository.save(newField);
        return new FieldResponse(
                savedField.getId(), savedField.getCapacity(), savedField.getCost()
        );
    }

    public List<TimeSlotsResponse> getFieldEvents(Short fieldId, LocalDate date) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT get_field_events_fn(
                            CAST(? AS date),
                            CAST(? AS smallint)
                        ) AS slots
                        """,
                (rs, rowNum) -> {
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


    public int bookEvent(Short fieldId, EventBookingRequest request) {

        var field = fieldRepository.findById(fieldId).orElseThrow(() -> new NotFoundException("Field not found"));


        BigDecimal totalAmount =
                field.getCost().multiply(
                        BigDecimal.valueOf(field.getCapacity())
                );

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

        if (request.discounted() != null) {
            amountPaid = amountPaid.subtract(request.discounted());
        }

        var event = EventBookings.builder().field(field).eventStatus(request.eventStatus().getValue())
                .remaining(remaining).eventKey(request.generatedCode())
                .eventStart(request.startTime()).build();

        var bookingPayment = EventBookingPayment.builder().event(event).payerId(request.payerId())
                .receivedById(request.receivedById()).paymentMethod(request.paymentMethod()).merchantNumber(request.merchantNumber()).amountPaid(amountPaid).discountAmount(request.discounted()).build();
        event.getBookingPayments().add(bookingPayment);
        var savedEvent = eventBookingRepository.save(event);
        return savedEvent.getId();
    }
}

