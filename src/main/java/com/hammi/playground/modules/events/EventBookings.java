package com.hammi.playground.modules.events;

import com.hammi.playground.modules.fields.Field;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "event_end", nullable = false)
    private LocalDateTime eventEnd;
}