package com.graminbank.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class YearlySettlementResponse {
    private String year;
    private BigDecimal totalDeposits;
    private BigDecimal totalLoans;
    private BigDecimal totalDepositInterest;
    private BigDecimal totalLoanInterest;
    private BigDecimal netProfit;
    private LocalDate settlementDate;
}
