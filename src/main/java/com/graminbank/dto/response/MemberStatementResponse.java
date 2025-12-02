package com.graminbank.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberStatementResponse {
    private String memberName;
    private String phone;
    private List<DepositResponse> deposits;
    private List<LoanResponse> loans;
    private BigDecimal totalDeposits;
    private BigDecimal totalLoans;
    private BigDecimal netPosition;
}