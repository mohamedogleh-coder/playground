package com.hammi.playground.modules.stadium;
//import org.postgresql.util.;

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

    public List<StadiumsListResponse> getAllStadiums() {
        String query = "SELECT * FROM get_stadiums_view";
        return jdbcTemplate.query(query, (rs, _) -> new StadiumsListResponse(
                UUID.fromString(rs.getString("id")),
                rs.getString("stadium_name"),
                rs.getDouble("longitude"),
                rs.getDouble("latitude"),
                rs.getString("profile_url"),
                rs.getShort("extra_time"),
                rs.getShort("num_of_fields")
        ));
    }

    public List<FilteredStadiumsResponse> filterEventsResponse(LocalDateTime time, Double latitude, Double longitude, Integer capacity) {
        return jdbcTemplate.query("SELECT * FROM filter_events_fn(CAST(? as timestamp),? ,?,CAST(? as smallint),?);", (rs, _) -> new FilteredStadiumsResponse(rs.getString("stadium_id"), rs.getString("stadium_name"),
                rs.getInt("extra_time"), rs.getDouble("distance"), rs.getInt("field_id"), rs.getInt("capacity"), rs.getBigDecimal("field_cost"),
                rs.getDouble("longitude"), rs.getDouble("latitude")), time, latitude, longitude, capacity, null);
    }
}