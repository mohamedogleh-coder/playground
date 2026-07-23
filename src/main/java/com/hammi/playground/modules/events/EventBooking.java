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
public class EventBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false, foreignKey = @ForeignKey(name = "event_bookings_field_id_fkey"))
    private Field field;

    @Column(name = "event_start", nullable = false)
    private LocalDateTime eventStart;

    @Column(name = "event_end", nullable = false)
    private LocalDateTime eventEnd;

    @Column(name = "extra_time", nullable = false)
    private Short extraTime;

    @Column(name = "event_key", length = 4)
    private String eventKey;

    @Column(name = "payment_status", nullable = false, length = 10)
    private String paymentStatus;

    @Column(name = "event_status", nullable = false, length = 10)
    private String eventStatus;

    @Column(name = "remaining", nullable = false, precision = 12, scale = 2)
    private BigDecimal remaining;

    @Column(name = "description", length = 50)
    private String description;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "event")
    private List<EventBookingPayment> bookingPayments = new ArrayList<>();

}