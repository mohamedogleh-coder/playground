package com.hammi.playground.modules.merchants;

import com.hammi.playground.modules.stadium.Stadium;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "stadium_merchants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "stadium_merchant_unq",
                        columnNames = {
                                "stadium_id",
                                "merchant_number",
                                "provider_id"
                        }
                )
        }
)
public class StadiumMerchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "merchant_number", nullable = false, length = 20)
    private String merchantNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;
}