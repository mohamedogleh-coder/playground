package com.hammi.playground.modules.expanses;

import com.hammi.playground.modules.fields.TimeSlotsResponse;
import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stadiums/{stadiumId}/expanses")
public class ExpansesController {

    private final ExpansesService expansesService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpanseResponse>>> getStadiumExpanses(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok(new ApiResponse<>(expansesService.getAllExpanses(stadiumId)));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<ExpanseResponse>> addExpanse(@PathVariable UUID stadiumId, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(expansesService.addExpanse(stadiumId, request)));
    }

}
