package com.graminbank.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaskedDepositResponse {
    private UUID id;
    private String memberName; // Masked
    private BigDecimal amount;
    private LocalDate depositDate;
    private String status;
}