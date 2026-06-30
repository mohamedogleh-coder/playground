package com.hammi.playground.modules.stadium.repo;

import com.hammi.playground.modules.stadium.entity.Stadium;
import com.hammi.playground.modules.stadium.entity.StadiumWorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StadiumWorkingOursRepository extends JpaRepository<StadiumWorkingDay, Short> {
}
