package com.hammi.playground.modules.stadium.repo;

import com.hammi.playground.modules.stadium.dto.StadiumResponse;
import com.hammi.playground.modules.stadium.entity.Stadium;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StadiumRepository extends CrudRepository<Stadium, UUID> {

    @Query("SELECT s FROM Stadium s JOIN  FETCH s.fields WHERE s.id=:stadiumId")
    Optional<Stadium> findStadiumWithFields(@Param("stadiumId") UUID stadiumId);

    @Query("SELECT s FROM Stadium s JOIN  FETCH s.workingDays WHERE s.id=:stadiumId")
    Optional<Stadium> findStadiumWithWorkingDays(@Param("stadiumId") UUID stadiumId);



}
