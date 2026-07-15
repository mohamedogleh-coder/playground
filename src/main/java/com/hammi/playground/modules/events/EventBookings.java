package com.hammi.playground.modules.events;

import com.hammi.playground.modules.fields.Field;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "event_bookings")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventBookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    @EqualsAndHashCode.Include
    private Field field;

    @Column(name = "event_key")
    private Integer eventKey;

    @Column(name = "event_start", nullable = false)
    @EqualsAndHashCode.Include
    private LocalDateTime eventStart;

    @Column(name = "remaining", nullable = false)
    @EqualsAndHashCode.Include
    private BigDecimal remaining;

    @Column(name = "event_status", nullable = false)
    @EqualsAndHashCode.Include
    private String eventStatus;

    @Column(name = "extra_time", nullable = false)
    @EqualsAndHashCode.Include
    private Short extraTime;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "event")
    private List<EventBookingPayment> bookingPayments = new ArrayList<>();

    @Column(name = "event_end", nullable = false)
    private LocalDateTime eventEnd;
}