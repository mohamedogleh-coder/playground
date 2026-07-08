package com.hammi.playground.modules.stadium;

import com.hammi.playground.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/staduims")
@RequiredArgsConstructor
public class StadiumController {
    private final StadiumService stadiumService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StadiumsListResponse>>> getAllStadiums() {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.getAllStadiums()));
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
