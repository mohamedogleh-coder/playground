package com.hammi.playground.modules.working_days;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkingDaysRepository extends JpaRepository<StadiumWorkingDay, Short> {
}
