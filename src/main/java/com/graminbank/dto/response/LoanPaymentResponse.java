package com.graminbank.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LoanPaymentResponse {
    private UUID id;
    private UUID loanId;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private BigDecimal discountApplied;
    private String notes;
    private LocalDateTime createdAt;
}