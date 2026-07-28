package com.hammi.playground.modules.expanses;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpanseResponse(
        Integer expanseId,
        String expenseTitle,
        String description,
        BigDecimal totalAmount,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime expenseDate) {
}
