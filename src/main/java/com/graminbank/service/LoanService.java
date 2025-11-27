package com.graminbank.service;

import com.graminbank.dto.request.LoanRequest;
import com.graminbank.dto.request.LoanUpdateRequest;
import com.graminbank.dto.request.LoanPaymentRequest;
import com.graminbank.dto.request.LoanClosureRequest;
import com.graminbank.dto.response.LoanResponse;
import com.graminbank.dto.response.LoanPaymentResponse;
import com.graminbank.exception.BusinessException;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.model.Loan;
import com.graminbank.model.LoanPayment;
import com.graminbank.model.Member;
import com.graminbank.repository.LoanRepository;
import com.graminbank.repository.LoanPaymentRepository;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public LoanResponse createLoan(LoanRequest request) {
        log.info("Creating loan for member: {}", request.getMemberId());

        if (request.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Loan amount must be greater than 0");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setLoanAmount(request.getLoanAmount());
        loan.setLoanDate(request.getLoanDate());
        loan.setInterestRate(new BigDecimal("5.0"));
        loan.setFinancialYear(InterestCalculator.getFinancialYearFromDate(request.getLoanDate()));
        loan.setStatus("ACTIVE");
        loan.setRemainingAmount(request.getLoanAmount());

        Loan savedLoan = loanRepository.save(loan);
        return convertToResponse(savedLoan);
    }

    @Transactional
    public LoanResponse updateLoan(UUID loanId, LoanUpdateRequest request) {
        log.info("Updating loan: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new BusinessException("Can only edit active loans");
        }

        if (request.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Loan amount must be greater than 0");
        }

        loan.setLoanAmount(request.getLoanAmount());
        loan.setLoanDate(request.getLoanDate());
        loan.setFinancialYear(InterestCalculator.getFinancialYearFromDate(request.getLoanDate()));

        // Recalculate remaining amount
        loan.setRemainingAmount(request.getLoanAmount().subtract(loan.getPaidAmount()));

        if (request.getMemberId() != null && !request.getMemberId().equals(loan.getMember().getId())) {
            Member newMember = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
            loan.setMember(newMember);
        }

        Loan updatedLoan = loanRepository.save(loan);
        return convertToResponse(updatedLoan);
    }

    @Transactional
    public LoanPaymentResponse addPayment(UUID loanId, LoanPaymentRequest request) {
        log.info("Adding payment to loan: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new BusinessException("Can only add payments to active loans");
        }

        if (request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be greater than 0");
        }

        // Calculate current total (principal + interest)
        BigDecimal currentInterest = InterestCalculator.calculateLoanInterest(
                loan.getLoanAmount(),
                loan.getLoanDate(),
                request.getPaymentDate()
        );
        BigDecimal totalOwed = loan.getLoanAmount().add(currentInterest);

        // Apply discount if provided
        BigDecimal discount = request.getDiscountAmount() != null ?
                request.getDiscountAmount() : BigDecimal.ZERO;

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Discount cannot be negative");
        }

        // Create payment record
        LoanPayment payment = new LoanPayment();
        payment.setLoan(loan);
        payment.setPaymentAmount(request.getPaymentAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setDiscountApplied(discount);
        payment.setNotes(request.getNotes());
        payment.setCreatedBy("admin"); // Can be updated with actual user

        loanPaymentRepository.save(payment);

        // Update loan totals
        loan.setPaidAmount(loan.getPaidAmount().add(request.getPaymentAmount()));
        loan.setDiscountAmount(loan.getDiscountAmount().add(discount));

        BigDecimal totalPaidWithDiscount = loan.getPaidAmount().add(loan.getDiscountAmount());
        loan.setRemainingAmount(totalOwed.subtract(totalPaidWithDiscount));

        // Check if fully paid
        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus("CLOSED");
            loan.setReturnDate(request.getPaymentDate());
            loan.setInterestAmount(currentInterest);
            loan.setTotalRepayment(loan.getLoanAmount().add(currentInterest));
            loan.setRemainingAmount(BigDecimal.ZERO);
        }

        loanRepository.save(loan);

        return convertPaymentToResponse(payment, loan);
    }

    public List<LoanPaymentResponse> getPaymentHistory(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        List<LoanPayment> payments = loanPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId);
        return payments.stream()
                .map(payment -> convertPaymentToResponse(payment, loan))
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanResponse closeLoan(UUID loanId, LoanClosureRequest request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new BusinessException("Loan is already closed or settled");
        }

        BigDecimal interest = InterestCalculator.calculateLoanInterest(
                loan.getLoanAmount(),
                loan.getLoanDate(),
                request.getReturnDate()
        );

        BigDecimal totalRepayment = loan.getLoanAmount().add(interest);
        BigDecimal discount = request.getDiscountAmount() != null ?
                request.getDiscountAmount() : BigDecimal.ZERO;

        // If closing with full payment
        if (request.getPaymentAmount() != null) {
            LoanPayment finalPayment = new LoanPayment();
            finalPayment.setLoan(loan);
            finalPayment.setPaymentAmount(request.getPaymentAmount());
            finalPayment.setPaymentDate(request.getReturnDate());
            finalPayment.setDiscountApplied(discount);
            finalPayment.setNotes("Final payment - Loan closed");
            finalPayment.setCreatedBy("admin");
            loanPaymentRepository.save(finalPayment);

            loan.setPaidAmount(loan.getPaidAmount().add(request.getPaymentAmount()));
        }

        loan.setDiscountAmount(loan.getDiscountAmount().add(discount));
        loan.setInterestAmount(interest);
        loan.setTotalRepayment(totalRepayment);
        loan.setStatus("CLOSED");
        loan.setReturnDate(request.getReturnDate());
        loan.setRemainingAmount(BigDecimal.ZERO);

        Loan closedLoan = loanRepository.save(loan);
        return convertToResponse(closedLoan);
    }

    public Page<LoanResponse> getLoansByStatus(String status, Pageable pageable) {
        return loanRepository.findByStatus(status, pageable)
                .map(this::convertToResponseWithCurrentInterest);
    }

    public LoanResponse getLoanById(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        return convertToResponseWithCurrentInterest(loan);
    }

    public List<Loan> getLoansByMember(UUID memberId) {
        return loanRepository.findByMemberId(memberId);
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

    @Transactional
    public Loan carryForwardLoan(Loan oldLoan, String newFinancialYear, LocalDate carryForwardDate) {
        log.info("Carrying forward loan {} to new financial year {}", oldLoan.getId(), newFinancialYear);

        BigDecimal interest = InterestCalculator.calculateLoanInterest(
                oldLoan.getLoanAmount(),
                oldLoan.getLoanDate(),
                carryForwardDate
        );

        oldLoan.setInterestAmount(interest);
        oldLoan.setTotalRepayment(oldLoan.getLoanAmount().add(interest));
        oldLoan.setStatus("CARRIED_FORWARD");
        oldLoan.setReturnDate(carryForwardDate);
        loanRepository.save(oldLoan);

        BigDecimal newLoanAmount = oldLoan.getLoanAmount().add(interest).subtract(oldLoan.getPaidAmount());

        Loan newLoan = new Loan();
        newLoan.setMember(oldLoan.getMember());
        newLoan.setLoanAmount(newLoanAmount);
        newLoan.setLoanDate(carryForwardDate.plusDays(1));
        newLoan.setInterestRate(new BigDecimal("5.0"));
        newLoan.setFinancialYear(newFinancialYear);
        newLoan.setStatus("ACTIVE");
        newLoan.setCarriedForwardFrom(oldLoan);
        newLoan.setRemainingAmount(newLoanAmount);

        return loanRepository.save(newLoan);
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
        response.setCarriedForward(loan.getCarriedForwardFrom() != null);
        response.setPaidAmount(loan.getPaidAmount());
        response.setDiscountAmount(loan.getDiscountAmount());
        response.setRemainingAmount(loan.getRemainingAmount());
        return response;
    }

    private LoanResponse convertToResponseWithCurrentInterest(Loan loan) {
        LoanResponse response = convertToResponse(loan);

        if ("ACTIVE".equals(loan.getStatus())) {
            BigDecimal currentInterest = InterestCalculator.calculateLoanInterest(
                    loan.getLoanAmount(),
                    loan.getLoanDate(),
                    LocalDate.now()
            );
            response.setCurrentInterest(currentInterest);

            BigDecimal currentTotal = loan.getLoanAmount().add(currentInterest);
            response.setCurrentTotal(currentTotal);

            BigDecimal totalPaidWithDiscount = loan.getPaidAmount().add(loan.getDiscountAmount());
            response.setCurrentRemaining(currentTotal.subtract(totalPaidWithDiscount));
        }

        return response;
    }

    private LoanPaymentResponse convertPaymentToResponse(LoanPayment payment, Loan loan) {
        LoanPaymentResponse response = new LoanPaymentResponse();
        response.setId(payment.getId());
        response.setLoanId(loan.getId());
        response.setPaymentAmount(payment.getPaymentAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setDiscountApplied(payment.getDiscountApplied());
        response.setNotes(payment.getNotes());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}