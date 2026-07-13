package com.hammi.playground.modules.stadium;
//import org.postgresql.util.;

import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.managers.StadiumManager;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumService {
    final JdbcTemplate jdbcTemplate;
    final StadiumRepository stadiumRepository;

    public List<StadiumsListResponse> getAllStadiums() {
        String query = "SELECT * FROM get_stadiums_view";
        return jdbcTemplate.query(query, (rs, _) -> new StadiumsListResponse(UUID.fromString(rs.getString("id")), rs.getString("stadium_name"), rs.getDouble("longitude"), rs.getDouble("latitude"), rs.getString("profile_url"), rs.getShort("extra_time"), rs.getShort("num_of_fields")));
    }

    public List<FilteredStadiumsResponse> filterEventsResponse(LocalDateTime time, Double latitude, Double longitude, Integer capacity) {
        return jdbcTemplate.query("SELECT * FROM filter_events_fn(CAST(? as timestamp),? ,?,CAST(? as smallint),?);", (rs, _) -> new FilteredStadiumsResponse(rs.getString("stadium_id"), rs.getString("stadium_name"), rs.getInt("extra_time"), rs.getDouble("distance"), rs.getInt("field_id"), rs.getInt("capacity"), rs.getBigDecimal("field_cost"), rs.getDouble("longitude"), rs.getDouble("latitude")), time, latitude, longitude, capacity, null);
    }

    public UUID registerStadium(StadiumRegRequest regRequest) {
        try {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

            Point point = regRequest.latitude() != null && regRequest.longitude() != null ? geometryFactory.createPoint(new Coordinate(regRequest.longitude(), regRequest.latitude())) : null;

            var stadium = Stadium.builder().stadiumName(regRequest.stadiumName()).extraTime(regRequest.extraTime()).profileUrl(regRequest.profileUrl()).location(point).build();

            var manager = StadiumManager.builder().managerId(regRequest.managerId()).stadium(stadium).build();

            stadium.getManagers().add(manager);

            var savedStadium = stadiumRepository.save(stadium);
            return savedStadium.getId();
        } catch (Exception e) {
            if (e.getMessage().contains("stadiums_stadium_name_key")) {
                throw new ApiException("Garoonka " + regRequest.stadiumName() + " hore ayuu u jiraa fadlan dooro magac kale");
            }
            throw e;
        }
    }

    public UUID updateStadium(UUID stadiumId, StadiumRegRequest regRequest) {
        try {
            var stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not found"));

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

            Point point = regRequest.latitude() != null && regRequest.longitude() != null ?
                    geometryFactory.createPoint(new Coordinate(regRequest.longitude(), regRequest.latitude())) : null;

            stadium.setStadiumName(regRequest.stadiumName());
            stadium.setExtraTime(regRequest.extraTime());
            stadium.setProfileUrl(regRequest.profileUrl());
            stadium.setLocation(point);

            var savedStadium = stadiumRepository.save(stadium);
            return savedStadium.getId();
        } catch (Exception e) {
            if (e.getMessage().contains("stadiums_stadium_name_key")) {
                throw new ApiException("Garoonka " + regRequest.stadiumName() + " hore ayuu u jiraa fadlan dooro magac kale");
            }
            throw e;
        }

    }

}