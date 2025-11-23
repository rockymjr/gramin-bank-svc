package com.graminbank.service;

import com.graminbank.dto.response.MemberStatementResponse;
import com.graminbank.dto.response.YearlySettlementResponse;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.model.Deposit;
import com.graminbank.model.FinancialYear;
import com.graminbank.model.Loan;
import com.graminbank.model.Member;
import com.graminbank.repository.DepositRepository;
import com.graminbank.repository.FinancialYearRepository;
import com.graminbank.repository.LoanRepository;
import com.graminbank.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberRepository memberRepository;
    private final DepositRepository depositRepository;
    private final LoanRepository loanRepository;
    private final FinancialYearRepository financialYearRepository;

    public MemberStatementResponse getMemberStatement(UUID memberId, String year) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (year == null) {
            year = String.valueOf(LocalDate.now().getYear());
        }

        List<Deposit> deposits = depositRepository.findByMemberId(memberId);
        List<Loan> loans = loanRepository.findByMemberId(memberId);

        BigDecimal totalDeposits = deposits.stream()
                .map(Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoans = loans.stream()
                .map(Loan::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MemberStatementResponse response = new MemberStatementResponse();
        response.setMemberName(member.getFirstName() + " " + member.getLastName());
        response.setPhone(member.getPhone());
        response.setDeposits(deposits);
        response.setLoans(loans);
        response.setTotalDeposits(totalDeposits);
        response.setTotalLoans(totalLoans);
        response.setNetPosition(totalDeposits.subtract(totalLoans));

        return response;
    }

    public YearlySettlementResponse getYearlySettlement(String year) {
        if (year == null) {
            year = String.valueOf(LocalDate.now().getYear());
        }

        FinancialYear fy = financialYearRepository.findByYear(year)
                .orElseThrow(() -> new ResourceNotFoundException("Financial year not found"));

        YearlySettlementResponse response = new YearlySettlementResponse();
        response.setYear(fy.getYear());
        response.setTotalDeposits(fy.getTotalDeposits());
        response.setTotalLoans(fy.getTotalLoans());
        response.setTotalDepositInterest(fy.getTotalInterestEarned());
        response.setTotalLoanInterest(fy.getTotalInterestPaid());
        response.setNetProfit(fy.getNetBalance());
        response.setSettlementDate(fy.getSettlementDate());

        return response;
    }
}