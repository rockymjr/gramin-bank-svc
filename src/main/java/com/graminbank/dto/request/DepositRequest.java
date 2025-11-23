package com.graminbank.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class DepositRequest {

    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Minimum deposit is ₹100")
    private BigDecimal amount;

    @NotNull(message = "Deposit date is required")
    private LocalDate depositDate;
}