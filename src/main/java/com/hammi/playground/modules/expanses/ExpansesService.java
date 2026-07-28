package com.hammi.playground.modules.expanses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpansesService {
    private final ExpansesRepository expansesRepository;
    private final StadiumRepository stadiumRepository;


    public List<ExpanseResponse> getAllExpanses(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithExpanses(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not found"));

        return stadium.getExpanses().stream().map(e -> new ExpanseResponse(e.getId(), e.getExpenseTitle(),
                e.getDescription(), e.getTotalAmount(),
                e.getExpenseDate()));
    }

    public ExpanseResponse addExpanse(UUID stadiumId, ExpenseRequest request) {

        var stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not found"));

        var expanse = Expense.builder().expenseTitle(request.expenseTitle())
                .description(request.description()).totalAmount(request.totalAmount())
                .stadium(stadium).build();


        var savedExpanse = expansesRepository.save(expanse);

        return new ExpanseResponse(savedExpanse.getId(), savedExpanse.getExpenseTitle(),
                savedExpanse.getDescription(), savedExpanse.getTotalAmount(),
                savedExpanse.getExpenseDate());

    }


}
