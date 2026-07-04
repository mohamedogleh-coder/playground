package com.hammi.playground.modules.stadium.controllers;

import com.hammi.playground.modules.stadium.dto.FieldResponse;
import com.hammi.playground.modules.stadium.dto.FilterEventsResponse;
import com.hammi.playground.modules.stadium.dto.StadiumResponse;
import com.hammi.playground.modules.stadium.dto.WorkingDaysResponse;
import com.hammi.playground.modules.stadium.services.FieldsService;
import com.hammi.playground.modules.stadium.services.StadiumService;
import com.hammi.playground.modules.stadium.services.StadiumWorkingDaysService;
import com.hammi.playground.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/staduims")
@RequiredArgsConstructor
public class StadiumController {
    private final StadiumService stadiumService;
    private final StadiumWorkingDaysService workingDaysService;
    private final FieldsService fieldsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StadiumResponse>>> getAllStadiums() {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.getAllStadiums()));
    }

    @GetMapping("/{stadiumId}/fields")
    public ResponseEntity<ApiResponse<List<FieldResponse>>> getStadiumFields(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.getStadiumFields(stadiumId)));
    }

    @GetMapping("/{stadiumId}/working-days")
    public ResponseEntity<ApiResponse<List<WorkingDaysResponse>>> getStadiumWorkingDays(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok().body(new ApiResponse<>(workingDaysService.getStadiumWorkingDays(stadiumId)));
    }


    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<FilterEventsResponse>>> filterEventsResponse(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
                                                                                        LocalDateTime time,
                                                                                        @RequestParam(required = false) Double latitude,
                                                                                        @RequestParam(required = false) Double longitude,
                                                                                        @RequestParam Integer capacity
    ) {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.filterEventsResponse(time, latitude, longitude, capacity)));
    }
}
