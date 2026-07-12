package com.hammi.playground.modules.fields;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.events.EventBookingRequest;
import com.hammi.playground.modules.events.EventBookings;
import com.hammi.playground.modules.events.EventBookingRepository;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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

    public FieldEventsResponse getFieldEvents(Short fieldId, LocalDate date) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT 
                            field_id,
                            capacity,
                            cost,
                            slots::text
                        FROM get_field_events_fn(
                            CAST(? AS date),
                            CAST(? AS smallint)
                        )
                        """,
                (rs, rowNum) -> {
                    String slotsJson = rs.getString("slots");

                    List<TimeSlotsResponse> slots =
                            objectMapper.readValue(
                                    slotsJson,
                                    new TypeReference<List<TimeSlotsResponse>>() {
                                    }
                            );

                    return new FieldEventsResponse(
                            rs.getShort("field_id"),
                            rs.getShort("capacity"),
                            rs.getBigDecimal("cost"),
                            slots);
                },
                date,
                fieldId
        );
    }

    public Integer bookEvent(Short fieldId, EventBookingRequest request) {

        var field = fieldRepository.findById(fieldId).orElseThrow(() -> new NotFoundException("Field not found"));
        var event = EventBookings.builder().field(field).eventKey(request.generatedCode()).eventStart(request.startTime()).build();

        var savedEvent = eventBookingRepository.save(event);
        return savedEvent.getId();
    }
}

