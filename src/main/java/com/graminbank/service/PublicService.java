package com.graminbank.service;

import com.graminbank.dto.response.MaskedDepositResponse;
import com.graminbank.dto.response.MaskedLoanResponse;
import com.graminbank.dto.response.SummaryResponse;
import com.graminbank.model.Deposit;
import com.graminbank.model.Loan;
import com.graminbank.repository.DepositRepository;
import com.graminbank.repository.LoanRepository;
import com.graminbank.util.NameMaskingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicService {

    private final DepositRepository depositRepository;
    private final LoanRepository loanRepository;

    public SummaryResponse getSummary() {
        // Get ALL deposits and loans
        List<Deposit> allDeposits = depositRepository.findAll();
        List<Loan> allLoans = loanRepository.findAll();

        // === CASH INFLOWS ===
        // 1. Total collected from deposits (principal only)
        BigDecimal totalDepositCollected = allDeposits.stream()
                .map(Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Total loan repayments received (includes principal + interest from borrowers)
        BigDecimal totalLoanRepaymentsReceived = allLoans.stream()
                .map(Loan::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // === CASH OUTFLOWS ===
        // 3. Total loans disbursed (money given out as loans)
        BigDecimal totalLoansDisbursed = allLoans.stream()
                .map(Loan::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4a. Total deposit principal returned to depositors
        BigDecimal totalLoanRepaid = allLoans.stream()
                .filter(d -> "CLOSED".equals(d.getStatus()))
                .map(Loan::getTotalRepayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4a. Total deposit principal returned to depositors
        BigDecimal totalDepositPrincipalReturned = allDeposits.stream()
                .filter(d -> "RETURNED".equals(d.getStatus()) || "SETTLED".equals(d.getStatus()))
                .map(Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4b. Total deposit interest paid to depositors
        BigDecimal totalDepositInterestPaid = allDeposits.stream()
                .filter(d -> "RETURNED".equals(d.getStatus()) || "SETTLED".equals(d.getStatus()))
                .map(d -> d.getInterestEarned() != null ? d.getInterestEarned() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // === AVAILABLE BALANCE CALCULATION ===
        // Money In: Deposits collected + Loan repayments
        // Money Out: Loans disbursed + Deposit principal returned + Deposit interest paid
        BigDecimal availableBalance = totalDepositCollected
                .add(totalLoanRepaymentsReceived)
                .add(totalLoanRepaid)
                .subtract(totalLoansDisbursed)
                .subtract(totalDepositPrincipalReturned)
                .subtract(totalDepositInterestPaid);

        // === BANK PROFIT CALCULATION ===
        // Total interest received from loans (5% per month)
        BigDecimal totalLoanInterestReceived = allLoans.stream()
                .filter(l -> "CLOSED".equals(l.getStatus()) || "SETTLED".equals(l.getStatus()))
                .map(Loan::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Bank's profit = (5% loan interest received) - (2.5% deposit interest paid)
        BigDecimal bankProfit = totalLoanInterestReceived.subtract(totalDepositInterestPaid);

        // Active counts and amounts
        Long activeDepositsCount = depositRepository.countByStatus("ACTIVE");
        Long activeLoansCount = loanRepository.countByStatus("ACTIVE");
        BigDecimal activeDeposits = depositRepository.getTotalDepositsByStatus("ACTIVE");
        BigDecimal activeLoans = loanRepository.getTotalLoansByStatus("ACTIVE");

        SummaryResponse response = new SummaryResponse();
        response.setTotalDeposits(activeDeposits);
        response.setTotalLoans(activeLoans);
        response.setAvailableBalance(availableBalance);
        response.setBankProfit(bankProfit);
        response.setActiveDepositsCount(activeDepositsCount);
        response.setActiveLoansCount(activeLoansCount);
        response.setFinancialYear(String.valueOf(LocalDate.now().getYear()));

        return response;
    }

    public Page<MaskedDepositResponse> getMaskedDeposits(Pageable pageable) {
        return depositRepository.findAll(pageable)
                .map(this::convertToMaskedResponse);
    }

    public Page<MaskedLoanResponse> getMaskedLoans(Pageable pageable) {
        return loanRepository.findAll(pageable)
                .map(this::convertToMaskedResponse);
    }

    private MaskedDepositResponse convertToMaskedResponse(Deposit deposit) {
        MaskedDepositResponse response = new MaskedDepositResponse();
        response.setId(deposit.getId());
        response.setMemberName(NameMaskingUtil.maskName(
                deposit.getMember().getFirstName(),
                deposit.getMember().getLastName()
        ));
        response.setAmount(deposit.getAmount());
        response.setDepositDate(deposit.getDepositDate());
        response.setStatus(deposit.getStatus());
        return response;
    }

    private MaskedLoanResponse convertToMaskedResponse(Loan loan) {
        MaskedLoanResponse response = new MaskedLoanResponse();
        response.setId(loan.getId());
        response.setMemberName(NameMaskingUtil.maskName(
                loan.getMember().getFirstName(),
                loan.getMember().getLastName()
        ));
        response.setLoanAmount(loan.getLoanAmount());
        response.setLoanDate(loan.getLoanDate());
        response.setStatus(loan.getStatus());
        return response;
    }
}