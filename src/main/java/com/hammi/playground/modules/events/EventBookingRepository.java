package com.hammi.playground.modules.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventBookingRepository extends JpaRepository<EventBookings, Integer> {
     @Query("SELECT e FROM EventBookings  e JOIN FETCH e.bookingPayments WHERE e.id=:eventId")
     Optional<EventBookings> getEventWithEventPayments(@Param("eventId") Integer eventId);
}
