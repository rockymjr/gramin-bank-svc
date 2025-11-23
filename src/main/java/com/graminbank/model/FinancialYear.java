package com.graminbank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_years")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialYear {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "year", nullable = false, unique = true, length = 4)
    private String year;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "total_deposits", precision = 15, scale = 2)
    private BigDecimal totalDeposits = BigDecimal.ZERO;

    @Column(name = "total_loans", precision = 15, scale = 2)
    private BigDecimal totalLoans = BigDecimal.ZERO;

    @Column(name = "total_interest_earned", precision = 15, scale = 2)
    private BigDecimal totalInterestEarned = BigDecimal.ZERO;

    @Column(name = "total_interest_paid", precision = 15, scale = 2)
    private BigDecimal totalInterestPaid = BigDecimal.ZERO;

    @Column(name = "net_balance", precision = 15, scale = 2)
    private BigDecimal netBalance = BigDecimal.ZERO;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;
}