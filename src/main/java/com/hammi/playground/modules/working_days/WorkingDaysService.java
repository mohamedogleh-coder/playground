package com.hammi.playground.modules.working_days;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class WorkingDaysService {
    private final StadiumRepository stadiumRepository;
    private final WorkingDaysRepository workingDaysRepository;

    public List<WorkingDaysResponse> getStadiumWorkingDays(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithWorkingDays(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
        return stadium.getWorkingDays().stream().map((day -> new WorkingDaysResponse(day.getId(), convertToDayOfTheWeek(day.getDayOfWeek()), day.getOpeningTime(), day.getClosingTime(), day.getIsOpen()))).toList();
    }

    public WorkingDaysResponse updateWorkingDay(Short workingDayId, WorkingDaysRequest request) {
        var workingDay = workingDaysRepository.findById(workingDayId).orElseThrow(() -> new NotFoundException("Working Day not exists"));
        workingDay.setOpeningTime(request.openingTime());
        workingDay.setClosingTime(request.closingTime());
        workingDay.setIsOpen(request.isOpen());

        workingDaysRepository.save(workingDay);
        return new WorkingDaysResponse(workingDay.getId(),
                convertToDayOfTheWeek(workingDay.getDayOfWeek()),
                workingDay.getOpeningTime(),
                workingDay.getClosingTime(),
                workingDay.getIsOpen());
    }

    @Transactional
    public List<WorkingDaysResponse> updateStadiumWorkingDays(UUID stadiumId, WorkingDaysRequestList requestList) {
        try {
            var stadium = stadiumRepository.findStadiumWithWorkingDays(stadiumId)
                    .orElseThrow(() -> new NotFoundException("Stadium not exists"));

            Map<Short, StadiumWorkingDay> existingDaysMap = stadium.getWorkingDays().stream()
                    .collect(Collectors.toMap(StadiumWorkingDay::getId, Function.identity()));


            requestList.workingDays().forEach(request -> {
                StadiumWorkingDay existingDay = existingDaysMap.get(request.id());
                if (existingDay != null) {
                    System.out.println("This changed");
                    if (!request.openingTime().equals(existingDay.getOpeningTime())
                            || !request.closingTime().equals(existingDay.getClosingTime())
                            || !request.isOpen().equals(existingDay.getIsOpen())) {

                        existingDay.setOpeningTime(request.openingTime());
                        existingDay.setClosingTime(request.closingTime());
                        existingDay.setIsOpen(request.isOpen());
                    } else {
                        System.out.println("Nothing is changed");
                    }
                } else {
                    System.out.println("Day not exists");
                }
            });

            stadiumRepository.save(stadium);

            return stadium.getWorkingDays().stream()
                    .map(day -> new WorkingDaysResponse(
                            day.getId(),
                            convertToDayOfTheWeek(day.getDayOfWeek()),
                            day.getOpeningTime(),
                            day.getClosingTime(),
                            day.getIsOpen()
                    ))
                    .toList();
        } catch (Exception e) {
            if (e.getMessage().contains("fn_check_working_day_conflicts")) {
                throw new ApiException(e.getMessage());
            }
            throw e;
        }

    }

    public String convertToDayOfTheWeek(Short data) {
        if (data == null) {
            return null;
        }
        return switch (data) {
            case 1 -> "Salasa";
            case 2 -> "Isniin";
            case 3 -> "Arbaca";
            case 4 -> "Khamiis";
            case 5 -> "Jimce";
            case 6 -> "Sabti";
            case 7 -> "Axad";
            default ->
                    throw new IllegalArgumentException("Nambarka database-ka ku jira maaha maalin usbuuc oo sax ah (waa inuu ahaado 1-7): " + data);
        };
    }

}
