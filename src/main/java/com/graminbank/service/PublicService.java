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

        // Calculate total collected from deposits
        BigDecimal totalDepositCollected = allDeposits.stream()
                .map(Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total loan payments received (paid_amount from all loans)
        BigDecimal totalLoanPaymentsReceived = allLoans.stream()
                .map(Loan::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total returned to depositors (principal + interest for returned/settled deposits)
        BigDecimal totalDepositReturned = allDeposits.stream()
                .filter(d -> "RETURNED".equals(d.getStatus()) || "SETTLED".equals(d.getStatus()))
                .map(d -> {
                    BigDecimal principal = d.getAmount();
                    BigDecimal interest = d.getInterestEarned() != null ? d.getInterestEarned() : BigDecimal.ZERO;
                    return principal.add(interest);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Available Balance = (Total Deposits + Loan Payments Received) - Deposits Returned
        BigDecimal availableBalance = totalDepositCollected
                .add(totalLoanPaymentsReceived)
                .subtract(totalDepositReturned);

        // Active counts and amounts
        Long activeDepositsCount = depositRepository.countByStatus("ACTIVE");
        Long activeLoansCount = loanRepository.countByStatus("ACTIVE");
        BigDecimal activeDeposits = depositRepository.getTotalDepositsByStatus("ACTIVE");
        BigDecimal activeLoans = loanRepository.getTotalLoansByStatus("ACTIVE");

        SummaryResponse response = new SummaryResponse();
        response.setTotalDeposits(activeDeposits);
        response.setTotalLoans(activeLoans);
        response.setAvailableBalance(availableBalance);  // FIXED CALCULATION
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