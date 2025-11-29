package com.graminbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class LoanRequest {

    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "0.01", message = "Loan amount must be greater than 0")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan date is required")
    private LocalDate loanDate;

    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%")
    @DecimalMax(value = "100", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate = new BigDecimal("5.0");

    private String notes;
}