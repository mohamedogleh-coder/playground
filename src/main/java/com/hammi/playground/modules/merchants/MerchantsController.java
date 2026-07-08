package com.hammi.playground.modules.merchants;

import com.hammi.playground.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stadiums/{stadiumId}/merchants")
public class MerchantsController {

    private final MerchantsService merchantsService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<MerchantResponse>>> addMerchants(@PathVariable UUID stadiumId, @RequestBody @Valid MerchantsListRequest request) {
        return ResponseEntity.ok().body(new ApiResponse<>(merchantsService.addMerchants(stadiumId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MerchantResponse>>> getStadiumMerchants(@PathVariable UUID stadiumId) {
        return ResponseEntity.ok().body(new ApiResponse<>(merchantsService.getStadiumMerchants(stadiumId)));
    }

    @PutMapping("/{merchantId}")
    public ResponseEntity<ApiResponse<MerchantResponse>> updateMerchant(@PathVariable UUID stadiumId, @PathVariable Short merchantId, @RequestBody @Valid MerchantRequest request) {
        return ResponseEntity.ok().body(new ApiResponse<>(merchantsService.updateMerchant(stadiumId, merchantId, request)));
    }

    @DeleteMapping("/{merchantId}")
    public ResponseEntity<ApiResponse<Void>> deleteMerchant(@PathVariable UUID stadiumId, @PathVariable Short merchantId) {
        merchantsService.deleteMerchant(stadiumId, merchantId);
        return ResponseEntity.ok().body(new ApiResponse<>(null));
    }
}
