package com.hammi.playground.modules.stadium;

import com.hammi.playground.modules.fields.FieldRequest;
import com.hammi.playground.modules.fields.FieldResponse;
import com.hammi.playground.modules.working_days.WorkingDaysRequest;
import com.hammi.playground.modules.working_days.WorkingDaysRequestList;
import com.hammi.playground.modules.working_days.WorkingDaysResponse;
import com.hammi.playground.modules.fields.FieldsService;
import com.hammi.playground.modules.working_days.WorkingDaysService;
import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
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
    private final WorkingDaysService workingDaysService;
    private final FieldsService fieldsService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StadiumsListResponse>>> getAllStadiums() {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.getAllStadiums()));
    }

    @GetMapping("/{stadiumId}/fields")
    public ResponseEntity<ApiResponse<List<FieldResponse>>> getStadiumFields(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.getStadiumFields(stadiumId)));
    }

    @PostMapping("/{stadiumId}/addField")
    public ResponseEntity<ApiResponse<FieldResponse>> addField(@PathVariable UUID stadiumId, @RequestBody @Valid FieldRequest request) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.addField(stadiumId, request)));
    }

    @GetMapping("/{stadiumId}/workingDays")
    public ResponseEntity<ApiResponse<List<WorkingDaysResponse>>> getStadiumWorkingDays(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok().body(new ApiResponse<>(workingDaysService.getStadiumWorkingDays(stadiumId)));
    }

    @PostMapping("/{stadiumId}/addWorkingDays")
    public ResponseEntity<ApiResponse<List<WorkingDaysResponse>>> addWorkingDay(@PathVariable UUID stadiumId, @RequestBody @Valid WorkingDaysRequestList request
    ) {
        return ResponseEntity.ok()
                .body(new ApiResponse<>(
                        workingDaysService.addWorkingDay(stadiumId, request)
                ));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<FilteredStadiumsResponse>>> filterEventsResponse(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
                                                                                            LocalDateTime time,
                                                                                            @RequestParam(required = false) Double latitude,
                                                                                            @RequestParam(required = false) Double longitude,
                                                                                            @RequestParam Integer capacity
    ) {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumService.filterEventsResponse(time, latitude, longitude, capacity)));
    }
}
