package com.hammi.playground.modules.stadium.repo;

import com.hammi.playground.modules.stadium.entity.Field;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FieldRepository extends CrudRepository<Field, Short> {
    @Query(value = "SELECT generate_time_slots(CAST(:fieldId AS smallint), CAST(:date AS date))", nativeQuery = true)
    String getRawBookingTimeSlots(@Param("fieldId") Short fieldId, @Param("date") LocalDate date);
}
