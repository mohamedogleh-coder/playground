package com.hammi.playground.modules.expanses;

import com.hammi.playground.modules.stadium.Stadium;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "expense_title", nullable = false, length = 50)
    private String expenseTitle;

    @Column(length = 100)
    private String description;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "expense_date")
    private LocalDateTime expenseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    @PrePersist
    public void prePersist() {
        if (expenseDate == null) {
            expenseDate = LocalDateTime.now();
        }
    }
}