package com.graminbank.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class LoanResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private BigDecimal loanAmount;
    private LocalDate loanDate;
    private String status;
    private LocalDate returnDate;
    private BigDecimal interestAmount; //Total Interest
    private BigDecimal totalRepayment; // Total Paid
    private Boolean carriedForward; // Remove this

    // Payment tracking
    private BigDecimal paidAmount;
    private BigDecimal discountAmount;
    private BigDecimal remainingAmount;

    // For active loans - current interest till today
    private BigDecimal currentInterest;
    private BigDecimal currentTotal;
    private BigDecimal currentRemaining;

    private BigDecimal interestRate;
    private Integer durationDays;
    private Integer durationMonths;
    private String notes;
}