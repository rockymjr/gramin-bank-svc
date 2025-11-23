package com.graminbank.dto.response;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SummaryResponse {
    private BigDecimal totalDeposits;
    private BigDecimal totalLoans;
    private BigDecimal availableBalance;
    private Long activeDepositsCount;
    private Long activeLoansCount;
    private String financialYear;
}
