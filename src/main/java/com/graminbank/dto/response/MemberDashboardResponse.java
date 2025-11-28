package com.graminbank.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MemberDashboardResponse {
    private String memberName;
    private String phone;

    // Total amounts (all time)
    private BigDecimal totalDeposited;
    private BigDecimal totalBorrowed;
    private BigDecimal totalDepositInterestEarned;
    private BigDecimal totalLoanInterestPaid;

    // Current amounts (active only)
    private BigDecimal currentDeposits;
    private BigDecimal currentLoans;
    private BigDecimal currentDepositInterest;
    private BigDecimal currentLoanInterest;

    // Lists
    private List<DepositSummary> deposits;
    private List<LoanSummary> loans;

    @Data
    public static class DepositSummary {
        private String depositDate;
        private BigDecimal amount;
        private BigDecimal interestEarned;
        private BigDecimal currentInterest;
        private String status;
    }

    @Data
    public static class LoanSummary {
        private String loanDate;
        private BigDecimal amount;
        private BigDecimal interestPaid;
        private BigDecimal currentInterest;
        private BigDecimal paidAmount;
        private BigDecimal remainingAmount;
        private String status;
    }
}