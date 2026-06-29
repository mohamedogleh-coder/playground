package com.hammi.playground.modules.stadium.services;
//import org.postgresql.util.;

import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.dto.FieldResponse;
import com.hammi.playground.modules.stadium.dto.StadiumResponse;
import com.hammi.playground.modules.stadium.repo.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    final JdbcTemplate jdbcTemplate;

    public List<FieldResponse> getStadiumFields(UUID stadiumId) {
        var stadium = stadiumRepository.findStadiumWithFields(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not exists"));
        return stadium.getFields().stream().map((field -> new FieldResponse(field.getId(), field.getCapacity(), field.getCost()))).toList();
    }


    public List<StadiumResponse> getAllStadiums() {
        String query = """
                SELECT s.id,
                       stadium_name,
                       extra_time,
                       profile_url,
                       ST_X(location::geometry) AS longitude,
                       ST_Y(location::geometry) AS latitude,
                       count(f.id)              as num_of_fields
                FROM stadiums s
                         JOIN fields f on s.id = f.stadium_id
                group by s.id, stadium_name, location, location;
                """;

        return jdbcTemplate.query(query, (rs, _) -> new StadiumResponse(
                UUID.fromString(rs.getString("id")),
                rs.getString("stadium_name"),
                rs.getDouble("longitude"),
                rs.getDouble("latitude"),
                rs.getString("profile_url"),
                rs.getShort("extra_time"),
                rs.getShort("num_of_fields")
        ));


    }
}