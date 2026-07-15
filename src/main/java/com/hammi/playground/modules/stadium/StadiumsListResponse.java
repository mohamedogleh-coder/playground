package com.hammi.playground.modules.stadium;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StadiumsListResponse {
    private UUID id;
    private String stadiumName;
    private Double latitude;
    private Double longitude;
    private String profileUrl;
    private short extraTime;
    private boolean halfBooking;
    private short numberOfFields;
}
