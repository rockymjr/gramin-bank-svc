package com.graminbank.service;

import com.graminbank.dto.request.MemberLoginRequest;
import com.graminbank.dto.response.DepositResponse;
import com.graminbank.dto.response.LoanResponse;
import com.graminbank.dto.response.MemberAuthResponse;
import com.graminbank.dto.response.MemberDashboardResponse;
import com.graminbank.exception.AuthenticationException;
import com.graminbank.model.Member;
import com.graminbank.repository.DepositRepository;
import com.graminbank.repository.LoanRepository;
import com.graminbank.repository.MemberRepository;
import com.graminbank.util.DepositMapper;
import com.graminbank.util.InterestCalculator;
import com.graminbank.util.JwtUtil;
import com.graminbank.util.LoanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static com.graminbank.util.BankConstants.ACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final DepositRepository depositRepository;
    private final LoanRepository loanRepository;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService; // NEW: Inject the helper service

    public MemberAuthResponse authenticate(MemberLoginRequest request) {
        Member member = memberRepository.findByPhoneAndIsActiveTrue(request.getPhone())
                .orElseThrow(() -> new AuthenticationException("Invalid phone number or member not active"));

        // Check if member is blocked
        if (member.isCurrentlyBlocked()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            String blockedUntilStr = member.getBlockedUntil().format(formatter);
            throw new AuthenticationException(
                    "Account is blocked due to multiple failed login attempts. " +
                            "Please contact admin or try again after " + blockedUntilStr
            );
        }

        if (member.getPin() == null || member.getPin().isEmpty()) {
            throw new AuthenticationException("PIN not set for this member. Please contact admin.");
        }

        // Check PIN
        if (!member.getPin().equals(request.getPin())) {
            // Record failed attempt in separate transaction (will commit even if we throw exception)
            try {
                loginAttemptService.recordFailedAttempt(member.getId());
            } catch (Exception e) {
                log.error("Error recording failed login attempt for member: {}", member.getId(), e);
            }

            // Reload member to get updated attempt count from database
            member = loginAttemptService.getMember(member.getId());

            int remainingAttempts = Math.max(0, 3 - member.getFailedLoginAttempts());

            if (member.isCurrentlyBlocked()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
                String blockedUntilStr = member.getBlockedUntil().format(formatter);
                throw new AuthenticationException(
                        "Invalid PIN. Account is now blocked until " + blockedUntilStr +
                                ". Please contact admin to unblock."
                );
            }

            throw new AuthenticationException(
                    "Invalid PIN. " + remainingAttempts + " attempt(s) remaining before account is blocked."
            );
        }

        // Successful login - reset failed attempts in separate transaction
        try {
            loginAttemptService.recordSuccessfulLogin(member.getId());
        } catch (Exception e) {
            log.error("Error recording successful login for member: {}", member.getId(), e);
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

        List<DepositResponse> deposits = depositRepository.findByMemberId(memberId).stream()
                .map(DepositMapper::convertToResponseWithCurrentInterest).toList();
        List<LoanResponse> loans = loanRepository.findByMemberId(memberId).stream()
                .map(LoanMapper::convertToResponseWithCurrentInterest).toList();

        MemberDashboardResponse response = new MemberDashboardResponse();
        response.setMemberName(member.getFirstName() + " " + member.getLastName());
        response.setPhone(member.getPhone());

        // Calculate totals
        BigDecimal totalDeposited = deposits.stream()
                .map(DepositResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBorrowed = loans.stream()
                .map(LoanResponse::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDepositInterest = deposits.stream()
                .filter(d -> !ACTIVE.equals(d.getStatus()))
                .map(DepositResponse::getInterestEarned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoanInterest = loans.stream()
                .filter(l -> !ACTIVE.equals(l.getStatus()))
                .map(LoanResponse::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Current amounts (active only)
        BigDecimal currentDeposits = deposits.stream()
                .filter(d -> ACTIVE.equals(d.getStatus()))
                .map(DepositResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentLoans = loans.stream()
                .filter(l -> ACTIVE.equals(l.getStatus()))
                .map(LoanResponse::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate current interest for active items
        BigDecimal currentDepositInterest = deposits.stream()
                .filter(d -> ACTIVE.equals(d.getStatus()))
                .map(d -> InterestCalculator.calculateDepositInterest(
                        d.getAmount(), d.getDepositDate(), LocalDate.now()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentLoanInterest = loans.stream()
                .filter(l -> ACTIVE.equals(l.getStatus()))
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
        response.setDeposits(deposits);
        response.setLoans(loans);
        return response;
    }
}