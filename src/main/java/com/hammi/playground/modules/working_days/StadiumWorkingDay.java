package com.hammi.playground.modules.working_days;
import com.hammi.playground.modules.stadium.Stadium;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "stadium_working_days",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "stadium_day_unique",
                        columnNames = {"stadium_id", "day_of_week"}
                )
        }
)
public class StadiumWorkingDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private Boolean isOpen = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;
}