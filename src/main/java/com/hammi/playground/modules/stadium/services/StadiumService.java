package com.hammi.playground.modules.stadium.services;
//import org.postgresql.util.;

import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.stadium.dto.FieldResponse;
import com.hammi.playground.modules.stadium.dto.StadiumResponse;
import com.hammi.playground.modules.stadium.dto.WorkingDaysResponse;
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
    final JdbcTemplate jdbcTemplate;

    public List<StadiumResponse> getAllStadiums() {
        String query = "SELECT * FROM get_stadiums_view";
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