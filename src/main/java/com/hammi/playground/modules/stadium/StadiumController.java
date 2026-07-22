package com.hammi.playground.modules.stadium;

import com.hammi.playground.modules.events.EventsBookedSummery;
import com.hammi.playground.modules.fields.FieldRequest;
import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stadiums")
@RequiredArgsConstructor
public class StadiumController {
    private final StadiumService stadiumService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StadiumsListResponse>>> getAllStadiums() {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.getAllStadiums()));
    }


    @GetMapping("/{stadiumId}/events/{bookingDate}")
    public ResponseEntity<ApiResponse<List<EventsBookedSummery>>> getStadiumBookedEvents(
            @PathVariable UUID stadiumId,
            @PathVariable LocalDate bookingDate
    ) {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.getEventsBookingSpecificDate(stadiumId, bookingDate)));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StadiumResponse>> registerStadium(@RequestPart("request") @Valid StadiumRegRequest regRequest,
                                                                        @RequestPart(value = "profile", required = false) MultipartFile profile) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(stadiumService.registerStadium(regRequest, profile)));
    }

    @PutMapping("/{stadiumId}")
    public ResponseEntity<ApiResponse<StadiumResponse>> updateStadium(@PathVariable UUID stadiumId, @Valid @RequestBody StadiumRegRequest regRequest) {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.updateStadium(stadiumId, regRequest)));
    }


    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<FilteredStadiumsResponse>>> filterEventsResponse(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
                                                                                            LocalDateTime time,
                                                                                            @RequestParam(required = false) Double latitude,
                                                                                            @RequestParam(required = false) Double longitude,
                                                                                            @RequestParam Integer capacity) {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.filterEventsResponse(time, latitude, longitude, capacity)));
    }
}
