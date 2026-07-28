package com.hammi.playground.modules.expanses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ExpenseRequest(

        @NotBlank(message = "Expense title is required")
        String expenseTitle,

        String description,

        @NotNull(message = "Total amount is required")
        BigDecimal totalAmount

) {
}