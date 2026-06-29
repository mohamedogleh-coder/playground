package com.hammi.playground.modules.stadium.entity;

 import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Column(name = "event_key")
    private Integer eventKey;

    @Column(name = "event_start", nullable = false)
    private LocalDateTime eventStart;

    @Column(name = "event_end", nullable = false)
    private LocalDateTime eventEnd;


}