package com.graminbank.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class DepositResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private BigDecimal amount;
    private LocalDate depositDate;
    private String status;
    private BigDecimal interestEarned;
    private BigDecimal totalAmount;
}