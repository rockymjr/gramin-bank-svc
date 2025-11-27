package com.graminbank.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class InterestCalculator {

    private static final BigDecimal DEPOSIT_MONTHLY_RATE = new BigDecimal("2.5");
    private static final BigDecimal LOAN_MONTHLY_RATE = new BigDecimal("5.0");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Calculate deposit interest on monthly basis
     * 1-30 days = 1 month, 31-60 days = 2 months, etc.
     * Formula: (Principal × Rate × Months) / 100
     */
    public static BigDecimal calculateDepositInterest(BigDecimal principal, LocalDate startDate, LocalDate endDate) {
        if (principal == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            return BigDecimal.ZERO;
        }

        // Calculate months: 1-30 days = 1 month, 31-60 = 2 months, etc.
        int months = (int) Math.ceil(days / 30.0);
        BigDecimal monthsBd = new BigDecimal(months);

        // (P × R × M) / 100
        BigDecimal interest = principal
                .multiply(DEPOSIT_MONTHLY_RATE)
                .multiply(monthsBd)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        return interest;
    }

    /**
     * Calculate loan interest on monthly basis
     * 1-30 days = 1 month, 31-60 days = 2 months, etc.
     * Formula: (Principal × Rate × Months) / 100
     */
    public static BigDecimal calculateLoanInterest(BigDecimal principal, LocalDate startDate, LocalDate endDate) {
        if (principal == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            return BigDecimal.ZERO;
        }

        // Calculate months: 1-30 days = 1 month, 31-60 = 2 months, etc.
        int months = (int) Math.ceil(days / 30.0);
        BigDecimal monthsBd = new BigDecimal(months);

        // (P × R × M) / 100
        BigDecimal interest = principal
                .multiply(LOAN_MONTHLY_RATE)
                .multiply(monthsBd)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        return interest;
    }

    /**
     * Get current financial year in format "2024-25"
     */
    public static String getCurrentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        if (month >= 4) {
            // April to December: current year to next year
            return year + "-" + String.format("%02d", (year + 1) % 100);
        } else {
            // January to March: previous year to current year
            return (year - 1) + "-" + String.format("%02d", year % 100);
        }
    }

    /**
     * Get financial year from a specific date
     */
    public static String getFinancialYearFromDate(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();

        if (month >= 4) {
            return year + "-" + String.format("%02d", (year + 1) % 100);
        } else {
            return (year - 1) + "-" + String.format("%02d", year % 100);
        }
    }
}