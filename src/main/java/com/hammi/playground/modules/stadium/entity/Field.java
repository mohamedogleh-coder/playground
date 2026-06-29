package com.hammi.playground.modules.stadium.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "field")
    private List<Event> events = new ArrayList<>();
}