package com.hammi.playground.modules.fields;

import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
