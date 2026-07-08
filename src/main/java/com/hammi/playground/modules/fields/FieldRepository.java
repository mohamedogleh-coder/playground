package com.hammi.playground.modules.fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FieldRepository extends JpaRepository<Field, Short> {
    @Query(value = "SELECT generate_booking_time_seq_fn(CAST(:fieldId AS smallint), CAST(:date AS date))", nativeQuery = true)
    String getRawBookingTimeSlots(@Param("fieldId") Short fieldId, @Param("date") LocalDate date);
}
