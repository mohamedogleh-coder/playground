package com.hammi.playground.modules.global;

import com.hammi.playground.modules.merchants.Provider;
import com.hammi.playground.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app")

public class AppControllers {
    private final AppService appService;

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<Provider>>> getFiledEvents() {
        return ResponseEntity.ok().body(new ApiResponse<>(appService.getMerchantProviders()));
    }
}
