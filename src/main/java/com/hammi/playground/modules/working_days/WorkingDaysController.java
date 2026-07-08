package com.hammi.playground.modules.working_days;

import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stadiums/{stadiumId}/days")
public class WorkingDaysController {
    private final WorkingDaysService workingDaysService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkingDaysResponse>>> getStadiumWorkingDays(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok().body(new ApiResponse<>(workingDaysService.getStadiumWorkingDays(stadiumId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<WorkingDaysResponse>>> addWorkingDay(@PathVariable UUID stadiumId, @RequestBody @Valid WorkingDaysRequestList request
    ) {
        return ResponseEntity.ok().body(new ApiResponse<>(workingDaysService.addWorkingDay(stadiumId, request)));
    }
}
