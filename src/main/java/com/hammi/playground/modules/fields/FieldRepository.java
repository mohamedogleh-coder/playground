package com.hammi.playground.modules.fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FieldRepository extends JpaRepository<Field, Short> {

    @Query(value = "SELECT get_field_events_fn(CAST(:date AS date),CAST(:fieldId AS smallint))", nativeQuery = true)
    String getFieldEvents(@Param("fieldId") Short fieldId, @Param("date") LocalDate date);
}
