package com.hammi.playground.modules.managers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StadiumManagerRepository extends JpaRepository<StadiumManager, UUID> {
    @Query("SELECT m FROM StadiumManager m JOIN FETCH m.stadium WHERE m.managerId=:managerId")
    Optional<StadiumManager> findStadiumManagerByManagerId(UUID managerId);
}
