package com.hammi.playground.modules.managers;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.StadiumRepository;
import com.hammi.playground.modules.stadium.StadiumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class StadiumManagerService {

    private final StadiumManagerRepository managerRepository;
    private final StadiumRepository stadiumRepository;


    public List<StadiumResponse> getStadiumByManagerId(UUID managerId) {
        var stadiums = managerRepository.findStadiumManagerByManagerId(managerId);

        return stadiums.stream().map(stadium ->
                new StadiumResponse(stadium.getStadium().getId(), stadium.getStadium().getStadiumName(),
                        stadium.getStadium().getLatitude(),
                        stadium.getStadium().getLongitude(),
                        stadium.getStadium().getExtraTime(),
                        stadium.getStadium().getProfileUrl())).toList();

    }

    public UUID addStadiumManager(UUID stadiumId, UUID managerId) {
        try {
            var stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not found"));
            var manager = StadiumManager.builder()
                    .managerId(managerId)
                    .stadium(stadium)
                    .build();
            managerRepository.save(manager);
            return stadiumId;
        } catch (Exception e) {
            if (e.getMessage().contains("stadium_manager_unq")) {
                throw new ApiException("User kan hore ayuu tirsan yahay garoonka");
            }
            throw e;
        }
    }
}
