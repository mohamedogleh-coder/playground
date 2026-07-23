package com.hammi.playground.modules.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Integer> {
    @Query("SELECT e FROM EventBooking e JOIN FETCH e.bookingPayments WHERE e.id=:eventId")
    Optional<EventBooking> getEventWithEventPayments(@Param("eventId") Integer eventId);


    @Query(value = "SELECT generate_booking_time_seq_fn(:fieldId, :date)", nativeQuery = true)
    String getTimeSlots(@Param("fieldId") Short fieldId, @Param("date") LocalDate date);

}
