package com.hammi.playground.modules.stadium.services;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.dto.*;
import com.hammi.playground.modules.stadium.repo.FieldRepository;
import com.hammi.playground.modules.stadium.repo.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
        return stadium.getFields().stream().map((field -> new FieldResponse(field.getId(), field.getCapacity(), field.getCost()))).toList();
    }

    public JsonNode getFiledEvents(Short fieldId, LocalDate date) {
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

}
