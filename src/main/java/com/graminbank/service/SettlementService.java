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

    @Transactional
    public void settleFinancialYear() {
        LocalDate settlementDate = LocalDate.now();
        String currentYear = String.valueOf(settlementDate.getYear());

        log.info("Starting yearly settlement for year: {}", currentYear);

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
            deposit.setSettledDate(settlementDate);
            depositRepository.save(deposit);

            totalDepositInterest = totalDepositInterest.add(interest);
            totalDepositAmount = totalDepositAmount.add(deposit.getAmount());
        }

        log.info("Settled {} deposits. Total principal: {}, Total interest: {}",
                activeDeposits.size(), totalDepositAmount, totalDepositInterest);

        // Settle all active loans (force closure)
        List<Loan> activeLoans = loanRepository.findByStatusAndFinancialYear("ACTIVE", currentYear);
        BigDecimal totalLoanInterest = BigDecimal.ZERO;
        BigDecimal totalLoanAmount = BigDecimal.ZERO;

        for (Loan loan : activeLoans) {
            BigDecimal interest = InterestCalculator.calculateLoanInterest(
                    loan.getLoanAmount(),
                    loan.getLoanDate(),
                    settlementDate
            );

            loan.setInterestAmount(interest);
            loan.setTotalRepayment(loan.getLoanAmount().add(interest));
            loan.setStatus("SETTLED");
            loan.setReturnDate(settlementDate);
            loanRepository.save(loan);

            totalLoanInterest = totalLoanInterest.add(interest);
            totalLoanAmount = totalLoanAmount.add(loan.getLoanAmount());
        }

        log.info("Settled {} loans. Total principal: {}, Total interest: {}",
                activeLoans.size(), totalLoanAmount, totalLoanInterest);

        // Create or update financial year record
        FinancialYear fy = financialYearRepository.findByYear(currentYear)
                .orElse(new FinancialYear());

        fy.setYear(currentYear);
        fy.setStartDate(LocalDate.of(settlementDate.getYear(), 1, 1));
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
        log.info("=== Settlement Summary ===");
        log.info("Year: {}", currentYear);
        log.info("Total Deposits: {} (Interest: {})", totalDepositAmount, totalDepositInterest);
        log.info("Total Loans: {} (Interest: {})", totalLoanAmount, totalLoanInterest);
        log.info("Net Profit: {}", fy.getNetBalance());
    }

    /**
     * Manual settlement trigger (for testing or admin use)
     */
    @Transactional
    public void manualSettlement(String year) {
        log.info("Manual settlement triggered for year: {}", year);

        List<Deposit> activeDeposits = depositRepository.findByStatusAndFinancialYear("ACTIVE", year);
        List<Loan> activeLoans = loanRepository.findByStatusAndFinancialYear("ACTIVE", year);

        LocalDate settlementDate = LocalDate.now();

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
            deposit.setSettledDate(settlementDate);
            depositRepository.save(deposit);

            totalDepositInterest = totalDepositInterest.add(interest);
            totalDepositAmount = totalDepositAmount.add(deposit.getAmount());
        }

        BigDecimal totalLoanInterest = BigDecimal.ZERO;
        BigDecimal totalLoanAmount = BigDecimal.ZERO;

        for (Loan loan : activeLoans) {
            BigDecimal interest = InterestCalculator.calculateLoanInterest(
                    loan.getLoanAmount(),
                    loan.getLoanDate(),
                    settlementDate
            );

            loan.setInterestAmount(interest);
            loan.setTotalRepayment(loan.getLoanAmount().add(interest));
            loan.setStatus("SETTLED");
            loan.setReturnDate(settlementDate);
            loanRepository.save(loan);

            totalLoanInterest = totalLoanInterest.add(interest);
            totalLoanAmount = totalLoanAmount.add(loan.getLoanAmount());
        }

        FinancialYear fy = financialYearRepository.findByYear(year)
                .orElse(new FinancialYear());

        fy.setYear(year);
        fy.setStartDate(LocalDate.of(Integer.parseInt(year), 1, 1));
        fy.setEndDate(settlementDate);
        fy.setIsActive(false);
        fy.setTotalDeposits(totalDepositAmount);
        fy.setTotalLoans(totalLoanAmount);
        fy.setTotalInterestEarned(totalDepositInterest);
        fy.setTotalInterestPaid(totalLoanInterest);
        fy.setNetBalance(totalLoanInterest.subtract(totalDepositInterest));
        fy.setSettlementDate(settlementDate);

        financialYearRepository.save(fy);

        log.info("Manual settlement completed for year: {}", year);
    }
}