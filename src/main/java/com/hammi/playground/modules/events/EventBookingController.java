package com.hammi.playground.modules.events;

import com.hammi.playground.modules.fields.TimeSlotsResponse;
import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fields/{fieldId}/events")
public class EventBookingController {

    private final EventBookingService eventService;


    @GetMapping("/slots/{eventDate}")
    public ResponseEntity<ApiResponse<List<TimeSlotsResponse>>> generateEventSlots(@PathVariable Short fieldId, @PathVariable LocalDate eventDate) {
        return ResponseEntity.ok(new ApiResponse<>(eventService.generateEventSlots(fieldId, eventDate)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Integer>> bookEvent(@PathVariable Short fieldId, @RequestBody @Valid EventBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(eventService.bookEvent(fieldId, request)));
    }

    @PostMapping("/{eventId}/half")
    public ResponseEntity<ApiResponse<Integer>> bookAnotherHalf(@PathVariable Integer eventId, @RequestBody @Valid EventTakeHalfRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(eventService.bookAnotherHalf(eventId, request)));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventInformationResponse>> getEventInformation(@PathVariable Integer eventId) {
        return ResponseEntity.ok().body(new ApiResponse<>(eventService.getEventInformation(eventId)));
    }
}
