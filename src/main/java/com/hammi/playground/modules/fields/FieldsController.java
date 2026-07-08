package com.hammi.playground.modules.fields;

import com.hammi.playground.modules.events.EventBookingRequest;
import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stadiums/{stadiumId}/fields")
class FieldsController {
    private final FieldsService fieldsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FieldResponse>>> getStadiumFields(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.getStadiumFields(stadiumId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FieldResponse>> addField(@PathVariable UUID stadiumId, @RequestBody @Valid FieldRequest request) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.addField(stadiumId, request)));
    }

    @GetMapping("/{fieldId}/events/{event_date}")
    public ResponseEntity<ApiResponse<JsonNode>> getFiledEvents(@PathVariable Short fieldId, @PathVariable("event_date") LocalDate eventDate) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.getFiledEvents(fieldId, eventDate)));
    }

    @PostMapping("/{fieldId}/book-event")
    public ResponseEntity<ApiResponse<Integer>> getFiledEvents(@PathVariable Short fieldId, @RequestBody @Valid EventBookingRequest request) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.bookEvent(fieldId, request)));
    }

}
