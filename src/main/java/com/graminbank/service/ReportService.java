package com.graminbank.service;

import com.graminbank.dto.response.DepositResponse;
import com.graminbank.dto.response.LoanResponse;
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
import com.graminbank.util.InterestCalculator;
import com.graminbank.util.DepositMapper;
import com.graminbank.util.LoanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberRepository memberRepository;
    private final DepositRepository depositRepository;
    private final LoanRepository loanRepository;
    private final FinancialYearRepository financialYearRepository;

    public MemberStatementResponse getMemberStatement(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        List<DepositResponse> deposits = depositRepository.findByMemberId(memberId).stream().map(DepositMapper::convertToResponseWithCurrentInterest).toList();
        List<LoanResponse> loans = loanRepository.findByMemberId(memberId).stream().map(LoanMapper::convertToResponseWithCurrentInterest).toList();

        BigDecimal totalDeposits = deposits.stream()
                .map(DepositResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoans = loans.stream()
                .map(LoanResponse::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        MemberStatementResponse response = new MemberStatementResponse();
        response.setMemberName(member.getFirstName() + " " + member.getLastName());
        response.setPhone(member.getPhone());
        response.setDeposits(deposits);
        response.setLoans(loans);
        response.setTotalDeposits(totalDeposits);
        response.setTotalLoans(totalLoans);
        response.setActiveDepositsWithInterest(
                deposits.stream()
                        .map(DepositResponse::getCurrentTotal)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        response.setActiveLoansWithInterest(
                loans.stream()
                        .map(LoanResponse::getCurrentTotal)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        return response;
    }

    public YearlySettlementResponse getYearlySettlement(String year) {
        if (year == null) {
            year = InterestCalculator.getCurrentFinancialYear();
        }

        // Try to find existing financial year data
        FinancialYear fy = financialYearRepository.findByYear(year).orElse(null);

        YearlySettlementResponse response = new YearlySettlementResponse();
        response.setYear(year);

        if (fy != null) {
            // Use existing financial year data
            response.setTotalDeposits(fy.getTotalDeposits());
            response.setTotalLoans(fy.getTotalLoans());
            response.setTotalDepositInterest(fy.getTotalInterestEarned());
            response.setTotalLoanInterest(fy.getTotalInterestPaid());
            response.setNetProfit(fy.getNetBalance());
            response.setSettlementDate(fy.getSettlementDate());
        } else {
            // Calculate from current data for the year
            List<Deposit> deposits = depositRepository.findByStatusAndFinancialYear("SETTLED", year);
            List<Loan> loans = loanRepository.findByStatusAndFinancialYear("SETTLED", year);

            BigDecimal totalDeposits = deposits.stream()
                    .map(Deposit::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalLoans = loans.stream()
                    .map(Loan::getLoanAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalDepositInterest = deposits.stream()
                    .map(Deposit::getInterestEarned)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalLoanInterest = loans.stream()
                    .map(Loan::getInterestAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.setTotalDeposits(totalDeposits);
            response.setTotalLoans(totalLoans);
            response.setTotalDepositInterest(totalDepositInterest);
            response.setTotalLoanInterest(totalLoanInterest);
            response.setNetProfit(totalLoanInterest.subtract(totalDepositInterest));
            response.setSettlementDate(null);
        }

        return response;
    }
}