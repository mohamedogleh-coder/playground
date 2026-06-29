package com.hammi.playground.modules.stadium.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.util.UUID;

@Data
@AllArgsConstructor
public class StadiumResponse {
    private UUID id;
    private String stadiumName;
    private Double latitude;
    private Double longitude;
    private String profileUrl;
    private short extraTime;
    private short numberOfFields;
}
