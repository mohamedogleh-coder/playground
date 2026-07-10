package com.hammi.playground.modules.managers;

import com.hammi.playground.modules.stadium.Stadium;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "stadium_managers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "stadium_manager_unq",
                        columnNames = {"manager_id", "stadium_id"}
                )
        }
)
public class StadiumManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Short id;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @Column(name = "date_joined", nullable = false)
    private LocalDate dateJoined;


    @PrePersist
    public void init() {
        dateJoined = LocalDate.now();
    }
}