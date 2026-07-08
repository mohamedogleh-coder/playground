package com.hammi.playground.modules.events;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventBookingRepository extends JpaRepository<EventBookings, Integer> {
}
