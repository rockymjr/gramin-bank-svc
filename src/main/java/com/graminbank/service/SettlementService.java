package com.graminbank.service;

import com.graminbank.model.Deposit;
import com.graminbank.model.FinancialYear;
import com.graminbank.model.Loan;
import com.graminbank.repository.DepositRepository;
import com.graminbank.repository.FinancialYearRepository;
import com.graminbank.repository.LoanRepository;
import com.graminbank.util.InterestCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final DepositRepository depositRepository;
    private final LoanRepository loanRepository;
    private final FinancialYearRepository financialYearRepository;
    private final LoanService loanService;

    @Transactional
    public void settleFinancialYear() {
        LocalDate settlementDate = LocalDate.now();
        String currentYear = InterestCalculator.getCurrentFinancialYear();

        log.info("Starting yearly settlement for financial year: {}", currentYear);

        // Settle all active deposits
        List<Deposit> activeDeposits = depositRepository.findByStatusAndFinancialYear("ACTIVE", currentYear);
        BigDecimal totalDepositInterest = BigDecimal.ZERO;
        BigDecimal totalDepositAmount = BigDecimal.ZERO;

        for (Deposit deposit : activeDeposits) {
            BigDecimal interest = InterestCalculator.calculateDepositInterest(
                    deposit.getAmount(),
                    deposit.getDepositDate(),
                    settlementDate
            );

            deposit.setInterestEarned(interest);
            deposit.setTotalAmount(deposit.getAmount().add(interest));
            deposit.setStatus("SETTLED");
            deposit.setReturnDate(settlementDate);
            depositRepository.save(deposit);

            totalDepositInterest = totalDepositInterest.add(interest);
            totalDepositAmount = totalDepositAmount.add(deposit.getAmount());
        }

        log.info("Settled {} deposits. Total principal: {}, Total interest: {}",
                activeDeposits.size(), totalDepositAmount, totalDepositInterest);

        // Handle active loans - carry forward to next year
        List<Loan> activeLoans = loanRepository.findByStatusAndFinancialYear("ACTIVE", currentYear);
        BigDecimal totalLoanInterest = BigDecimal.ZERO;
        BigDecimal totalLoanAmount = BigDecimal.ZERO;

        String nextFinancialYear = getNextFinancialYear(currentYear);

        for (Loan loan : activeLoans) {
            // Carry forward the loan to next year
            Loan carriedForwardLoan = loanService.carryForwardLoan(loan, nextFinancialYear, settlementDate);

            totalLoanInterest = totalLoanInterest.add(loan.getInterestAmount());
            totalLoanAmount = totalLoanAmount.add(loan.getLoanAmount());

            log.info("Carried forward loan {} with new amount {}",
                    loan.getId(), carriedForwardLoan.getLoanAmount());
        }

        log.info("Carried forward {} loans. Total principal: {}, Total interest: {}",
                activeLoans.size(), totalLoanAmount, totalLoanInterest);

        // Create or update financial year record
        FinancialYear fy = financialYearRepository.findByYear(currentYear)
                .orElse(new FinancialYear());

        fy.setYear(currentYear);
        fy.setStartDate(getFinancialYearStartDate(currentYear));
        fy.setEndDate(settlementDate);
        fy.setIsActive(false);
        fy.setTotalDeposits(totalDepositAmount);
        fy.setTotalLoans(totalLoanAmount);
        fy.setTotalInterestEarned(totalDepositInterest);
        fy.setTotalInterestPaid(totalLoanInterest);
        fy.setNetBalance(totalLoanInterest.subtract(totalDepositInterest));
        fy.setSettlementDate(settlementDate);

        financialYearRepository.save(fy);

        log.info("Financial year settlement completed. Net balance: {}", fy.getNetBalance());
    }

    private String getNextFinancialYear(String currentYear) {
        // "2024-25" -> "2025-26"
        String[] parts = currentYear.split("-");
        int year = Integer.parseInt(parts[0]);
        return (year + 1) + "-" + String.format("%02d", (year + 2) % 100);
    }

    private LocalDate getFinancialYearStartDate(String financialYear) {
        // "2024-25" -> 2024-04-01
        String[] parts = financialYear.split("-");
        int year = Integer.parseInt(parts[0]);
        return LocalDate.of(year, 4, 1);
    }
}