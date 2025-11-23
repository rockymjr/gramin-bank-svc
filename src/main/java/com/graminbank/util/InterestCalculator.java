package com.graminbank.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class InterestCalculator {

    private static final BigDecimal DEPOSIT_YEARLY_RATE = new BigDecimal("30"); // 2.5% monthly = 30% yearly
    private static final BigDecimal LOAN_YEARLY_RATE = new BigDecimal("60"); // 5% monthly = 60% yearly
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");

    /**
     * Calculate deposit interest using simple interest formula
     * Formula: (Principal × 30% × Days) / (100 × 365)
     *
     * @param principal The principal amount
     * @param startDate The deposit start date
     * @param endDate The settlement/end date
     * @return The calculated interest amount
     */
    public static BigDecimal calculateDepositInterest(BigDecimal principal, LocalDate startDate, LocalDate endDate) {
        if (principal == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal daysBd = new BigDecimal(days);

        // (P × R × T) / (100 × 365)
        BigDecimal interest = principal
                .multiply(DEPOSIT_YEARLY_RATE)
                .multiply(daysBd)
                .divide(HUNDRED.multiply(DAYS_IN_YEAR), 2, RoundingMode.HALF_UP);

        return interest;
    }

    /**
     * Calculate loan interest using simple interest formula
     * Formula: (Principal × 60% × Days) / (100 × 365)
     *
     * @param principal The loan principal amount
     * @param startDate The loan start date
     * @param endDate The repayment/settlement date
     * @return The calculated interest amount
     */
    public static BigDecimal calculateLoanInterest(BigDecimal principal, LocalDate startDate, LocalDate endDate) {
        if (principal == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal daysBd = new BigDecimal(days);

        // (P × R × T) / (100 × 365)
        BigDecimal interest = principal
                .multiply(LOAN_YEARLY_RATE)
                .multiply(daysBd)
                .divide(HUNDRED.multiply(DAYS_IN_YEAR), 2, RoundingMode.HALF_UP);

        return interest;
    }
}