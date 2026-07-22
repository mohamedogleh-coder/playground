package com.hammi.playground.modules.fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FieldRepository extends JpaRepository<Field, Short> {

//   @Query(value = "SELECT f FROM Field f LEFT JOIN FETCH f.fieldImages WHERE f.id = :fieldId")
////    Optional<Field> findFieldWithFieldImages(@Param("fieldId") Short fieldId);
////
////
//////    boolean existsByFieldIdAndEventStartGreaterThanEqual(
//////            Short fieldId,
//////            LocalDateTime eventStart
//////    );
//
//    @Query("""
//    SELECT DISTINCT f
//    FROM Field f
//    LEFT JOIN FETCH f.fieldImages
//    LEFT JOIN f.eventBookings e
//    WHERE f.id = :fieldId
//      AND (e IS NULL OR e.eventStart >= CURRENT_TIMESTAMP)
//""")
//    Optional<Field> findFieldWithUpcomingEvents(@Param("fieldId") Short fieldId);

//    @Query(value = """
//            SELECT  f
//                FROM Field f
//                LEFT JOIN FETCH f.fieldImages
//                LEFT JOIN fetch f.eventBookings e ON e.eventStart>=CURRENT_TIMESTAMP
//                WHERE f.id = :fieldId
//            """)
//    Optional<Field> findFieldWithUpcomingEvents(@Param("fieldId") Short fieldId);

//        LEFT JOIN FETCH f.eventBookings eb
//                    ON eb.eventStart >= CURRENT_TIMESTAMP
//    @Query(value = "SELECT get_field_events_fn(CAST(:date AS date),CAST(:fieldId AS smallint))", nativeQuery = true)
//    String getFieldEvents(@Param("fieldId") Short fieldId, @Param("date") LocalDate date);
}
