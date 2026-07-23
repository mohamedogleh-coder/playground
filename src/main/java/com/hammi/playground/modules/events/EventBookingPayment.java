package com.hammi.playground.modules.events;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventBookingPayment {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "event_id", nullable = false)
//    private EventBookings event;
//
//    @Column(name = "paid_user", nullable = false)
//    private UUID paidUser;
//
//    @Column(name = "received_by")
//    private UUID receivedBy;
//
//    @Column(name = "payment_method", nullable = false, length = 20)
//    private String paymentMethod;
//
//    @Column(name = "paid_by", length = 20)
//    private String paidBy;
//
//    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
//    private BigDecimal amountPaid;
//
//    @Builder.Default
//    @Column(name = "discounted", precision = 12, scale = 2)
//    private BigDecimal discounted = BigDecimal.ZERO;
//
//    @Builder.Default
//    @Column(name = "paid_at", nullable = false)
//    private LocalDateTime paidAt = LocalDateTime.now();


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

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "merchant_number", length = 20)
    private String merchantNumber;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "discounted", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discounted = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();
}