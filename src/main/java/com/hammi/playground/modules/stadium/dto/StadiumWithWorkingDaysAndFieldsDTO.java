package com.hammi.playground.modules.stadium.dto;

import java.time.LocalTime;

public record StadiumWithWorkingDaysAndFieldsDTO(
        LocalTime openTime,
        LocalTime closeTime,
        Short extraTime,
        Short fieldId,
        Boolean isOpen
) {
}
