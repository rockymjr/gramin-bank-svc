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
    @DecimalMin(value = "5000.00", message = "Minimum loan amount is ₹5,000")
    @DecimalMax(value = "20000.00", message = "Maximum loan amount is ₹20,000")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan date is required")
    private LocalDate loanDate;
}