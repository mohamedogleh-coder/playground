package com.hammi.playground.modules.events;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventBookingRepository extends JpaRepository<EventBookings, Integer> {
    @Query("SELECT e FROM EventBookings  e JOIN FETCH e.bookingPayments WHERE e.id=:eventId")
    Optional<EventBookings> getEventWithEventPayments(@Param("eventId") Integer eventId);


    boolean existsByField_IdAndEventEndGreaterThanEqual(Short fieldId, LocalDateTime eventStartIsGreaterThan);
}
