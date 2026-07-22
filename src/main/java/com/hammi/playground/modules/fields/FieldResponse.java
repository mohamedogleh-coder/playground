package com.hammi.playground.modules.fields;

import java.math.BigDecimal;
import java.util.List;

public record FieldResponse(
        short id,
        short capacity,
        BigDecimal cost,
        boolean stopBooking,
        List<String> fieldImages
) {
}

//
//record FieldImageResponse(Short imageId, String imagePath){
//
//}

