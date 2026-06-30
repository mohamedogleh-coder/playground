package com.hammi.playground.modules.stadium.controllers;

import com.hammi.playground.modules.stadium.dto.EventResponse;
import com.hammi.playground.modules.stadium.services.FieldsService;
import com.hammi.playground.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fields")
class FieldsController {

    private final FieldsService fieldsService;


    @GetMapping("/{fieldId}/events")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getFieldEvents(@PathVariable Short fieldId, @RequestParam("event_date") LocalDate eventDate) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.getFieldEvents(fieldId, eventDate)));
    }


    @GetMapping("/{fieldId}/events/json")
    public ResponseEntity<ApiResponse<JsonNode>> getFiledEventsJson(@PathVariable Short fieldId, @RequestParam("event_date") LocalDate eventDate) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.getFiledEventsJson(fieldId, eventDate)));
    }

}
