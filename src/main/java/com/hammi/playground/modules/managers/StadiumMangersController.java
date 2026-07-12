package com.hammi.playground.modules.managers;

import com.hammi.playground.modules.stadium.StadiumResponse;
import com.hammi.playground.modules.stadium.StadiumsListResponse;
import com.hammi.playground.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/managers")
public class StadiumMangersController {
    private final StadiumManagerService stadiumManagerService;

    @GetMapping("/{managerId}")
    public ResponseEntity<ApiResponse<List<StadiumResponse>>> getStadiumByManagerId(@PathVariable UUID managerId) {
        return ResponseEntity.ok().body(new ApiResponse<>(stadiumManagerService.getStadiumByManagerId(managerId)));
    }

    @PostMapping("/{managerId}/stadiums/{stadiumId}")
    public ResponseEntity<ApiResponse<UUID>> addStadiumManager(@PathVariable UUID stadiumId, @PathVariable UUID managerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(stadiumManagerService.addStadiumManager(stadiumId, managerId)));
    }

}
