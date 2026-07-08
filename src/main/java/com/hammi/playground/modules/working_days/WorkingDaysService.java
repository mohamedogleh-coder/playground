package com.hammi.playground.modules.working_days;

import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WorkingDaysService {
    private final StadiumRepository stadiumRepository;
    private final WorkingDaysRepository workingDaysRepository;

    public List<WorkingDaysResponse> getStadiumWorkingDays(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithWorkingDays(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
        return stadium.getWorkingDays().stream().map((day -> new WorkingDaysResponse(day.getId(), day.getDayOfWeek(), day.getOpeningTime(), day.getOpeningTime(), day.getIsOpen()))).toList();
    }


    public List<WorkingDaysResponse> addWorkingDay(UUID stadiumId, WorkingDaysRequestList requests) {

        var stadium = stadiumRepository.findStadiumWithWorkingDays(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium not exists"));

        return requests.workingDays().stream()
                .map(request -> stadium.getWorkingDays().stream()
                        .filter(day -> day.getDayOfWeek().equals(request.dayOfWeek()))
                        .findFirst()
                        .map(day -> new WorkingDaysResponse(
                                day.getId(),
                                day.getDayOfWeek(),
                                day.getOpeningTime(),
                                day.getClosingTime(),
                                day.getIsOpen()
                        ))
                        .orElseGet(() -> {

                            var workingDay = StadiumWorkingDay.builder()
                                    .dayOfWeek(request.dayOfWeek())
                                    .openingTime(request.openingTime())
                                    .closingTime(request.closingTime())
                                    .isOpen(request.isOpen())
                                    .stadium(stadium)
                                    .build();

                            var savedWorkingDay = workingDaysRepository.save(workingDay);

                            return new WorkingDaysResponse(
                                    savedWorkingDay.getId(),
                                    savedWorkingDay.getDayOfWeek(),
                                    savedWorkingDay.getOpeningTime(),
                                    savedWorkingDay.getClosingTime(),
                                    savedWorkingDay.getIsOpen()
                            );
                        }))
                .toList();
    }

//
//    public WorkingDaysResponse addWorkingDay(UUID stadiumId, WorkingDaysRequestList requests) {
//        var stadium = stadiumRepository.findStadiumWithWorkingDays(stadiumId)
//                .orElseThrow(() -> new NotFoundException("Stadium not exists"));
//
//        return stadium.getWorkingDays().stream()
//                .filter(day -> day.getDayOfWeek().equals(request.dayOfWeek()))
//                .findFirst()
//                .map(day -> new WorkingDaysResponse(
//                        day.getId(),
//                        day.getDayOfWeek(),
//                        day.getOpeningTime(),
//                        day.getClosingTime(),
//                        day.getIsOpen()
//                ))
//                .orElseGet(() -> {
//
//                    var workingDay = StadiumWorkingDay.builder()
//                            .dayOfWeek(request.dayOfWeek())
//                            .openingTime(request.openingTime())
//                            .closingTime(request.closingTime())
//                            .isOpen(request.isOpen())
//                            .stadium(stadium)
//                            .build();
//
//                    var savedWorkingDay = workingDaysRepository.save(workingDay);
//
//                    return new WorkingDaysResponse(
//                            savedWorkingDay.getId(),
//                            savedWorkingDay.getDayOfWeek(),
//                            savedWorkingDay.getOpeningTime(),
//                            savedWorkingDay.getClosingTime(),
//                            savedWorkingDay.getIsOpen()
//                    );
//                });
//    }


}
