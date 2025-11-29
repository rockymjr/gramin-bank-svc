package com.graminbank.service;


import com.graminbank.dto.request.MemberLoginRequest;
import com.graminbank.dto.response.MemberAuthResponse;
import com.graminbank.dto.response.MemberDashboardResponse;
import com.graminbank.exception.AuthenticationException;
import com.graminbank.model.Deposit;
import com.graminbank.model.Loan;
import com.graminbank.model.Member;
import com.graminbank.repository.DepositRepository;
import com.graminbank.repository.LoanRepository;
import com.graminbank.repository.MemberRepository;
import com.graminbank.util.InterestCalculator;
import com.graminbank.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final DepositRepository depositRepository;
    private final LoanRepository loanRepository;
    private final JwtUtil jwtUtil;

    public MemberAuthResponse authenticate(MemberLoginRequest request) {
        Member member = memberRepository.findByPhoneAndIsActiveTrue(request.getPhone())
                .orElseThrow(() -> new AuthenticationException("Invalid phone number or member not active"));

        if (member.getPin() == null || member.getPin().isEmpty()) {
            throw new AuthenticationException("PIN not set for this member. Please contact admin.");
        }

        if (!member.getPin().equals(request.getPin())) {
            throw new AuthenticationException("Invalid PIN");
        }

        String token = jwtUtil.generateToken("MEMBER_" + member.getId().toString());

        MemberAuthResponse response = new MemberAuthResponse();
        response.setToken(token);
        response.setMemberId(member.getId());
        response.setMemberName(member.getFirstName() + " " + member.getLastName());
        response.setPhone(member.getPhone());
        response.setExpiresIn(86400L);
        response.setIsOperator(member.getIsOperator());
        return response;
    }

    public MemberDashboardResponse getMemberDashboard(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Deposit> deposits = depositRepository.findByMemberId(memberId);
        List<Loan> loans = loanRepository.findByMemberId(memberId);

        MemberDashboardResponse response = new MemberDashboardResponse();
        response.setMemberName(member.getFirstName() + " " + member.getLastName());
        response.setPhone(member.getPhone());

        // Calculate totals
        BigDecimal totalDeposited = deposits.stream()
                .map(Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBorrowed = loans.stream()
                .map(Loan::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDepositInterest = deposits.stream()
                .filter(d -> !"ACTIVE".equals(d.getStatus()))
                .map(Deposit::getInterestEarned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoanInterest = loans.stream()
                .filter(l -> !"ACTIVE".equals(l.getStatus()))
                .map(Loan::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Current amounts (active only)
        BigDecimal currentDeposits = deposits.stream()
                .filter(d -> "ACTIVE".equals(d.getStatus()))
                .map(Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentLoans = loans.stream()
                .filter(l -> "ACTIVE".equals(l.getStatus()))
                .map(Loan::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate current interest for active items
        BigDecimal currentDepositInterest = deposits.stream()
                .filter(d -> "ACTIVE".equals(d.getStatus()))
                .map(d -> InterestCalculator.calculateDepositInterest(
                        d.getAmount(), d.getDepositDate(), LocalDate.now()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentLoanInterest = loans.stream()
                .filter(l -> "ACTIVE".equals(l.getStatus()))
                .map(l -> InterestCalculator.calculateLoanInterest(
                        l.getLoanAmount(), l.getLoanDate(), LocalDate.now()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setTotalDeposited(totalDeposited);
        response.setTotalBorrowed(totalBorrowed);
        response.setTotalDepositInterestEarned(totalDepositInterest);
        response.setTotalLoanInterestPaid(totalLoanInterest);
        response.setCurrentDeposits(currentDeposits);
        response.setCurrentLoans(currentLoans);
        response.setCurrentDepositInterest(currentDepositInterest);
        response.setCurrentLoanInterest(currentLoanInterest);

        // Map deposits
        response.setDeposits(deposits.stream().map(d -> {
            MemberDashboardResponse.DepositSummary ds = new MemberDashboardResponse.DepositSummary();
            ds.setDepositDate(d.getDepositDate().toString());
            ds.setAmount(d.getAmount());
            ds.setInterestEarned(d.getInterestEarned());
            ds.setStatus(d.getStatus());
            if ("ACTIVE".equals(d.getStatus())) {
                ds.setCurrentInterest(InterestCalculator.calculateDepositInterest(
                        d.getAmount(), d.getDepositDate(), LocalDate.now()));
            }
            return ds;
        }).collect(Collectors.toList()));

        // Map loans
        response.setLoans(loans.stream().map(l -> {
            MemberDashboardResponse.LoanSummary ls = new MemberDashboardResponse.LoanSummary();
            ls.setLoanDate(l.getLoanDate().toString());
            ls.setAmount(l.getLoanAmount());
            ls.setInterestPaid(l.getInterestAmount());
            ls.setPaidAmount(l.getPaidAmount());
            ls.setRemainingAmount(l.getRemainingAmount());
            ls.setStatus(l.getStatus());
            if ("ACTIVE".equals(l.getStatus())) {
                ls.setCurrentInterest(InterestCalculator.calculateLoanInterest(
                        l.getLoanAmount(), l.getLoanDate(), LocalDate.now()));
            }
            return ls;
        }).collect(Collectors.toList()));

        return response;
    }
}