package com.hammi.playground.modules.stadium;

import com.hammi.playground.config.SupabaseStorageService;
import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import com.hammi.playground.modules.events.EventsBookedSummery;
import com.hammi.playground.modules.managers.StadiumManager;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumService {
    final JdbcTemplate jdbcTemplate;
    final StadiumRepository stadiumRepository;
    private final SupabaseStorageService supabaseStorageService;

    public List<StadiumsListResponse> getAllStadiums() {
        String query = "SELECT * FROM get_stadiums_view";
        return jdbcTemplate.query(query, (rs, _) -> new StadiumsListResponse(UUID.fromString(rs.getString("id")), rs.getString("stadium_name"), rs.getDouble("longitude"), rs.getDouble("latitude"), rs.getString("profile_url"), rs.getShort("extra_time"), rs.getBoolean("half_booking"), rs.getShort("num_of_fields")));
    }

    public List<FilteredStadiumsResponse> filterEventsResponse(LocalDateTime time, Double latitude, Double longitude, Integer capacity) {
        return jdbcTemplate.query("SELECT * FROM filter_events_fn(CAST(? as timestamp),? ,?,CAST(? as smallint),?);", (rs, _) -> new FilteredStadiumsResponse(rs.getString("stadium_id"), rs.getString("stadium_name"), rs.getInt("extra_time"), rs.getDouble("distance"), rs.getInt("field_id"), rs.getInt("capacity"), rs.getBigDecimal("field_cost"), rs.getDouble("longitude"), rs.getDouble("latitude")), time, latitude, longitude, capacity, null);
    }

    public StadiumResponse registerStadium(StadiumRegRequest regRequest, MultipartFile profile) {
        try {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

            Point point = regRequest.latitude() != null && regRequest.longitude() != null ? geometryFactory.createPoint(new Coordinate(regRequest.longitude(), regRequest.latitude())) : null;

            var stadium = Stadium.builder().stadiumName(regRequest.stadiumName()).extraTime(regRequest.extraTime()).halfBooking(regRequest.halfBooking()).profileUrl(regRequest.profileUrl()).location(point).build();

            var manager = StadiumManager.builder().managerId(regRequest.managerId()).stadium(stadium).build();

            stadium.getManagers().add(manager);

            var savedStadium = stadiumRepository.save(stadium);

            String profilePath = null;
            if (profile != null && !profile.isEmpty()) {
                String folderPrefix = "stadiums/" + savedStadium.getId() + "/profile";

                profilePath = supabaseStorageService.uploadFile(profile, folderPrefix);

                savedStadium.setProfileUrl(profilePath);
                stadiumRepository.save(savedStadium);
            }

            return new StadiumResponse(savedStadium.getId(), savedStadium.getStadiumName(), savedStadium.getLatitude(), savedStadium.getLongitude(), stadium.getExtraTime(), savedStadium.getHalfBooking(), profilePath != null ? supabaseStorageService.getPublicUrl(profilePath) : null);

        } catch (IOException e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("stadiums_stadium_name_key")) {
                throw new ApiException("Garoonka " + regRequest.stadiumName() + " hore ayuu u jiraa fadlan dooro magac kale");
            }
            throw e;
        }
    }

    public StadiumResponse updateStadium(UUID stadiumId, StadiumRegRequest regRequest) {
        try {
            var stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not found"));

            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

            Point point = regRequest.latitude() != null && regRequest.longitude() != null ? geometryFactory.createPoint(new Coordinate(regRequest.longitude(), regRequest.latitude())) : null;

            stadium.setStadiumName(regRequest.stadiumName());
            stadium.setExtraTime(regRequest.extraTime());
            stadium.setProfileUrl(regRequest.profileUrl());
            stadium.setHalfBooking(regRequest.halfBooking());
            stadium.setLocation(point);

            var savedStadium = stadiumRepository.save(stadium);
            return new StadiumResponse(savedStadium.getId(), savedStadium.getStadiumName(), savedStadium.getLatitude(), savedStadium.getLongitude(), stadium.getExtraTime(), savedStadium.getHalfBooking(), stadium.getProfileUrl());
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("stadiums_stadium_name_key")) {
                throw new ApiException("Garoonka " + regRequest.stadiumName() + " hore ayuu u jiraa fadlan dooro magac kale");
            }
            throw e;
        }

    }

    public List<EventsBookedSummery> getEventBookingStatusSummaryByDateRange(
            UUID stadiumId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String query = """
                   SELECT
                       f.id AS field_id,
                       f.capacity,
                     COUNT(eb.id) FILTER (WHERE eb.event_status = 'pending') AS pending_events,
                     COUNT(eb.id) FILTER (WHERE eb.event_status = 'confirmed') AS confirmed_events,
                     COUNT(eb.id) FILTER (WHERE eb.event_status = 'completed') AS completed_events,
                     COUNT(eb.id) FILTER (WHERE eb.event_status = 'canceled') AS canceled_events
                   FROM public.fields f
                            LEFT JOIN public.event_bookings eb
                                      ON f.id = eb.field_id
                                          AND eb.event_start::date BETWEEN ? AND ?
                   WHERE f.stadium_id = ?
                   GROUP BY
                       f.id,
                       f.capacity
                """;
        return jdbcTemplate.query(
                query,
                (rs, _) -> new EventsBookedSummery(
                        rs.getShort("field_id"),
                        rs.getShort("capacity"),
                        rs.getShort("pending_events"),
                        rs.getShort("confirmed_events"),
                        rs.getShort("completed_events"),
                        rs.getShort("canceled_events")
                ),
                startDate,
                endDate,
                stadiumId
        );
    }

    public void deleteProfile(UUID stadiumId) {
        var stadium = stadiumRepository.findById(stadiumId).orElseThrow(() -> new NotFoundException("Stadium not found"));
        if (stadium.getProfileUrl() != null) {
            supabaseStorageService.deleteFile(stadium.getProfileUrl());
            stadium.setProfileUrl(null);
            stadiumRepository.save(stadium);
        }
    }

}