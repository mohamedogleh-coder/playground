package com.hammi.playground.modules.stadium.services;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.dto.*;
import com.hammi.playground.modules.stadium.repo.EventRepository;
import com.hammi.playground.modules.stadium.repo.FieldRepository;
import com.hammi.playground.modules.stadium.repo.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FieldsService {
    private final FieldRepository fieldRepository;
    private final ObjectMapper objectMapper;
    private final StadiumRepository stadiumRepository;
    private final EventRepository eventRepository;

    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
        return stadium.getFields().stream().map((field -> new FieldResponse(field.getId(), field.getCapacity(), field.getCost()))).toList();
    }

    public JsonNode getFiledEventsJson(Short fieldId, LocalDate date) {
        String rawJsonStr = fieldRepository.getRawBookingTimeSlots(fieldId, date);

        try {
            if (rawJsonStr != null) {
                return objectMapper.readTree(rawJsonStr);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }

        return objectMapper.createObjectNode();
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getFieldEvents(Short fieldId, LocalDate eventDate) {
        DayOfWeek dayOfWeek = eventDate.getDayOfWeek();

        var workingDay = fieldRepository.getFieldAndStadiumInfo(fieldId, dayOfWeek)
                .orElseThrow(() -> new NotFoundException("Field not found"));

        if (!workingDay.isOpen()) {
            throw new ApiException("Stadium not working on " + dayOfWeek);
        }

        LocalDateTime currentStart = LocalDateTime.of(eventDate, workingDay.openTime());
        LocalDateTime endBoundary = LocalDateTime.of(eventDate, workingDay.closeTime());

        var bookedEvents = eventRepository.findOverlappingEvents(fieldId, currentStart, endBoundary);

        List<EventResponse> slots = new ArrayList<>();
        Duration slotDuration = Duration.ofHours(1).plus(Duration.ofMinutes(workingDay.extraTime()));

        while (!currentStart.plus(slotDuration).isAfter(endBoundary)) {
            LocalDateTime currentEnd = currentStart.plus(slotDuration);
            final LocalDateTime finalStart = currentStart;

            Optional<EventDto> overlappingEvent = bookedEvents.stream()
                    .filter(event -> event.startTime().isBefore(currentEnd) && event.endTime().isAfter(finalStart))
                    .findFirst();

            boolean isAvailable = overlappingEvent.isEmpty();
            Integer eventId = isAvailable ? null : overlappingEvent.get().eventId();

            slots.add(new EventResponse(
                    finalStart,
                    currentEnd,
                    isAvailable,
                    eventId
            ));

            currentStart = currentEnd;
        }

        return slots;
    }

}
