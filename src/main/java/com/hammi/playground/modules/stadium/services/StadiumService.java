package com.hammi.playground.modules.stadium.services;
//import org.postgresql.util.;

import com.hammi.playground.modules.stadium.dto.FilterEventsResponse;
import com.hammi.playground.modules.stadium.dto.StadiumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public List<FilterEventsResponse> filterEventsResponse(LocalDateTime time, Double latitude, Double longitude, Integer capacity) {
        return jdbcTemplate.query("SELECT * FROM filter_events_fn(CAST(? as timestamp),? ,?,CAST(? as smallint),?);", (rs, _) -> new FilterEventsResponse(rs.getString("stadium_id"), rs.getString("stadium_name"),
                rs.getDouble("distance"),  rs.getInt("field_id"),rs.getInt("capacity"),rs.getBigDecimal("field_cost"),
                rs.getDouble("longitude"), rs.getDouble("latitude")), time, latitude, longitude, capacity,null);
    }
}