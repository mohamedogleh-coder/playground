package com.hammi.playground.modules.fields;

 import com.hammi.playground.modules.events.EventBooking;
 import com.hammi.playground.modules.stadium.Stadium;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(
        name = "fields"
)
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "capacity", nullable = false)
    private Short capacity;

    @Column(name = "stop_booking", nullable = false)
    private Boolean stopBooking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.PERSIST, mappedBy = "field")
    private List<EventBooking> eventBookings = new ArrayList<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "field")
    private Set<FieldImage> fieldImages = new LinkedHashSet<>();

}