package com.hammi.playground.modules.stadium.services;

import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.dto.WorkingDaysResponse;
import com.hammi.playground.modules.stadium.repo.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StadiumWorkingDaysService {
    private final StadiumRepository stadiumRepository;

    public List<WorkingDaysResponse> getStadiumWorkingDays(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithWorkingDays(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
        return stadium.getWorkingDays().stream().map((day -> new WorkingDaysResponse(day.getId(), day.getDayOfWeek(), day.getOpeningTime(), day.getOpeningTime(), day.getIsOpen()))).toList();
    }


}
