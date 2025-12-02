package com.graminbank.util;

import com.graminbank.dto.response.LoanPaymentResponse;
import com.graminbank.dto.response.LoanResponse;
import com.graminbank.model.Loan;
import com.graminbank.model.LoanPayment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanMapper {

    public static LoanResponse convertToResponse(Loan loan) {
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
        response.setPaidAmount(loan.getPaidAmount());
        response.setDiscountAmount(loan.getDiscountAmount());
        response.setRemainingAmount(loan.getRemainingAmount());
        InterestCalculator.DurationResult duration = InterestCalculator.calculateDuration(loan.getLoanDate(), loan.getReturnDate() != null ? loan.getReturnDate() : LocalDate.now());
        response.setDurationDays(duration.days);
        response.setDurationMonths(duration.months);
        response.setCurrentInterest(loan.getInterestAmount());
        response.setCurrentTotal(loan.getLoanAmount().add(loan.getInterestAmount()));
        return response;
    }

    public static LoanResponse convertToResponseWithCurrentInterest(Loan loan) {
        LoanResponse response = convertToResponse(loan);
        response.setInterestRate(loan.getInterestRate());
        response.setNotes(loan.getNotes());

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

    public static LoanPaymentResponse convertPaymentToResponse(LoanPayment payment, Loan loan) {
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
