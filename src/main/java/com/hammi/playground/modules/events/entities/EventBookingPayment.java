package com.hammi.playground.modules.events.entities;

import com.hammi.playground.modules.events.EventMerchantPayment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "event_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventBookingPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "event_payments_event_id_fkey"))
    private EventBooking event;

    @Column(name = "paid_user")
    private UUID paidUser;

    @Column(name = "received_by")
    private UUID receivedBy;

    @Column(name = "discounted", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discounted = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "payment")
    private List<EventMerchantPayment> merchantPayments = new ArrayList<>();

}