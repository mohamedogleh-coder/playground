package com.hammi.playground.modules.fields;

import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FieldResponse>> addField(
            @PathVariable UUID stadiumId,
            @RequestPart("request") @Valid FieldRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.addField(stadiumId, request, files)));
    }

    @PutMapping(path = "/{fieldId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FieldResponse>> updateField(
            @PathVariable UUID stadiumId,
            @PathVariable Short fieldId,
            @RequestPart("request") @Valid FieldRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.updateField(stadiumId, fieldId, request, files)));
    }

//
//    @DeleteMapping(path = "/{fieldId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse<FieldResponse>> updateField(
//            @PathVariable UUID stadiumId,
//            @PathVariable Short fieldId,
//            @RequestPart("request") @Valid FieldRequest request,
//            @RequestPart(value = "files", required = false) List<MultipartFile> files
//    ) {
//        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.updateField(stadiumId, fieldId, request, files)));
//    }


//    @PostMapping
//    public ResponseEntity<ApiResponse<FieldResponse>> addField(@PathVariable UUID stadiumId, @RequestBody @Valid FieldRequest request) {
//        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.addField(stadiumId, request)));
//    }
//
//    @PutMapping("/{fieldId}")
//    public ResponseEntity<ApiResponse<FieldResponse>> addField(@PathVariable UUID stadiumId, @PathVariable Short fieldId, @RequestBody @Valid FieldRequest request) {
//        return ResponseEntity.ok().body(new ApiResponse<>(fieldsService.updateField(stadiumId, fieldId, request)));
//    }
}
