package com.hammi.playground.modules.fields;

import com.hammi.playground.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/field/images")
public class FieldImageController {
    private final FieldsService fieldsService;

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFieldImage(@RequestParam String imagePath) {
        fieldsService.deleteImage(imagePath);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(null));
    }

}
