package com.graminbank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "loan_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate = new BigDecimal("5.0");

    @Column(name = "financial_year", nullable = false, length = 4)
    private String financialYear;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "interest_amount", precision = 12, scale = 2)
    private BigDecimal interestAmount = BigDecimal.ZERO;

    @Column(name = "total_repayment", precision = 12, scale = 2)
    private BigDecimal totalRepayment = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (financialYear == null) {
            financialYear = String.valueOf(LocalDate.now().getYear());
        }
    }
}