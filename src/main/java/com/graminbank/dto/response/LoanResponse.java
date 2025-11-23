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
    private BigDecimal interestAmount;
    private BigDecimal totalRepayment;
}