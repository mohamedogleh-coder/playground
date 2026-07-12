package com.hammi.playground.modules.stadium;

import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<ApiResponse<UUID>> registerStadium(@Valid @RequestBody StadiumRegRequest regRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(stadiumService.registerStadium(regRequest)));
    }

    @PutMapping("/{stadiumId}")
    public ResponseEntity<ApiResponse<UUID>> updateStadium(@PathVariable UUID stadiumId, @Valid @RequestBody StadiumRegRequest regRequest) {
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
