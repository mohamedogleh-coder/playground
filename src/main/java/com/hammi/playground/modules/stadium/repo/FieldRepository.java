package com.hammi.playground.modules.stadium.repo;

import com.hammi.playground.modules.stadium.dto.StadiumWithWorkingDaysAndFieldsDTO;
import com.hammi.playground.modules.stadium.entity.Field;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FieldRepository extends CrudRepository<Field, Short> {
    @Query(value = "SELECT generate_booking_time_seq_fn(CAST(:fieldId AS smallint), CAST(:date AS date))", nativeQuery = true)
    String getRawBookingTimeSlots(@Param("fieldId") Short fieldId, @Param("date") LocalDate date);

    @Query("SELECT new com.hammi.playground.modules.stadium.dto.StadiumWithWorkingDaysAndFieldsDTO(" +
            "d.openingTime, d.closingTime, s.extraTime, f.id, COALESCE(d.isOpen, false)) " + // Haddii d.isOpen la waayo, noqo false
            "FROM Field f " +
            "JOIN Stadium s ON s.id = f.stadium.id " +
            "LEFT JOIN StadiumWorkingDay d ON s.id = d.stadium.id AND d.dayOfWeek = :dayOfWeek " + // LEFT JOIN + Shuruuddii maalinta
            "WHERE f.id = :fieldId")
    Optional<StadiumWithWorkingDaysAndFieldsDTO> getFieldAndStadiumInfo(
            @Param("fieldId") Short fieldId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);



//    @Query("SELECT new com.hammi.playground.modules.stadium.dto.StadiumWithWorkingDaysAndFieldsDTO(" +
//            "d.openingTime, d.closingTime,s.extraTime,f.id,d.isOpen) " +
//            "FROM Field f " +
//            "JOIN Stadium s ON s.id = f.stadium.id " +
//            "LEFT JOIN StadiumWorkingDay d ON s.id = d.stadium.id " +
//            "WHERE f.id = :fieldId " +
//            "AND d.dayOfWeek = :dayOfWeek")
//    Optional<StadiumWithWorkingDaysAndFieldsDTO> getFieldAndStadiumInfo(
//            @Param("fieldId") Short fieldId,
//            @Param("dayOfWeek") DayOfWeek dayOfWeek);

}
