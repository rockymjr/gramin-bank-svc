package com.graminbank.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaskedLoanResponse {
    private UUID id;
    private String memberName; // Masked
    private BigDecimal loanAmount;
    private LocalDate loanDate;
    private String status;
}