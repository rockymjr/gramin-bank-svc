package com.graminbank.dto.response;

import com.graminbank.model.Deposit;
import com.graminbank.model.Loan;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberStatementResponse {
    private String memberName;
    private String phone;
    private List<Deposit> deposits;
    private List<Loan> loans;
    private BigDecimal totalDeposits;
    private BigDecimal totalLoans;
    private BigDecimal netPosition;
}