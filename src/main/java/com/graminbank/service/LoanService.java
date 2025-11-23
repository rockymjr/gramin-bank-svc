package com.graminbank.service;

import com.graminbank.dto.request.LoanRequest;
import com.graminbank.dto.response.LoanResponse;
import com.graminbank.exception.BusinessException;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.model.Loan;
import com.graminbank.model.Member;
import com.graminbank.repository.LoanRepository;
import com.graminbank.repository.MemberRepository;
import com.graminbank.util.InterestCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;

    private static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("5000");
    private static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("20000");

    @Transactional
    public LoanResponse createLoan(LoanRequest request) {
        log.info("Creating loan for member: {}", request.getMemberId());

        // Validate loan amount
        if (request.getLoanAmount().compareTo(MIN_LOAN_AMOUNT) < 0 ||
                request.getLoanAmount().compareTo(MAX_LOAN_AMOUNT) > 0) {
            throw new BusinessException("Loan amount must be between ₹5,000 and ₹20,000");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setLoanAmount(request.getLoanAmount());
        loan.setLoanDate(request.getLoanDate());
        loan.setInterestRate(new BigDecimal("5.0"));
        loan.setFinancialYear(String.valueOf(LocalDate.now().getYear()));
        loan.setStatus("ACTIVE");

        Loan savedLoan = loanRepository.save(loan);
        return convertToResponse(savedLoan);
    }

    public Page<LoanResponse> getActiveLoans(Pageable pageable) {
        return loanRepository.findByStatus("ACTIVE", pageable)
                .map(this::convertToResponse);
    }

    public List<Loan> getLoansByMember(UUID memberId) {
        return loanRepository.findByMemberId(memberId);
    }

    @Transactional
    public LoanResponse closeLoan(UUID loanId, LocalDate returnDate) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new BusinessException("Loan is already closed or settled");
        }

        BigDecimal interest = InterestCalculator.calculateLoanInterest(
                loan.getLoanAmount(),
                loan.getLoanDate(),
                returnDate
        );

        loan.setInterestAmount(interest);
        loan.setTotalRepayment(loan.getLoanAmount().add(interest));
        loan.setStatus("CLOSED");
        loan.setReturnDate(returnDate);

        Loan closedLoan = loanRepository.save(loan);
        return convertToResponse(closedLoan);
    }

    @Transactional
    public void settleLoan(Loan loan, LocalDate settlementDate) {
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
    }

    private LoanResponse convertToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setMemberId(loan.getMember().getId());
        response.setMemberName(loan.getMember().getFirstName() + " " + loan.getMember().getLastName());
        response.setLoanAmount(loan.getLoanAmount());
        response.setLoanDate(loan.getLoanDate());
        response.setStatus(loan.getStatus());
        response.setReturnDate(loan.getReturnDate());
        response.setInterestAmount(loan.getInterestAmount());
        response.setTotalRepayment(loan.getTotalRepayment());
        return response;
    }
}