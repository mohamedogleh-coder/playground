package com.hammi.playground.modules.stadium.repo;

import com.hammi.playground.modules.stadium.dto.EventDto;
import com.hammi.playground.modules.stadium.entity.Event;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
//    @Query("SELECT new com.hammi.playground.modules.stadium.dto.EventDto(e.id, e.eventKey, e.eventStart, e.eventEnd) " +
//            "FROM Event e " +
//            "WHERE e.field.id = :fieldId " +
//            "AND e.eventStart = :startTime " +
//            "AND e.eventEnd = :endTime")
//    List<EventDto> findAllByFieldAndExactTime(
//            @Param("fieldId") Short fieldId,
//            @Param("startTime") LocalDateTime startTime,
//            @Param("endTime") LocalDateTime endTime
//    );
@Query("SELECT new com.hammi.playground.modules.stadium.dto.EventDto(e.id, e.eventKey, e.eventStart, e.eventEnd) " +
        "FROM Event e " +
        "WHERE e.field.id = :fieldId " +
        "AND e.eventStart < :endTime " +  // Event-ku waa inuu bilaawdo ka hor inta uusan slot-ku dhammaan
        "AND e.eventEnd > :startTime")    // Event-ku waa inuu dhammaada ka dib marka uu slot-ku bilaawdo
List<EventDto> findOverlappingEvents(
        @Param("fieldId") Short fieldId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
);
}


