package com.graminbank.util;

import com.graminbank.dto.response.DepositResponse;
import com.graminbank.model.Deposit;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DepositMapper {

    private DepositMapper() {
    }

    public static DepositResponse convertToResponseWithCurrentInterest(Deposit deposit) {
        DepositResponse response = convertToResponse(deposit);

        if ("ACTIVE".equals(deposit.getStatus())) {
            BigDecimal currentInterest = InterestCalculator.calculateDepositInterest(
                    deposit.getAmount(),
                    deposit.getDepositDate(),
                    LocalDate.now()
            );
            response.setInterestEarned(currentInterest);
            response.setTotalAmount(deposit.getAmount().add(currentInterest));
        }

        return response;
    }

    public static DepositResponse convertToResponse(Deposit deposit) {
        DepositResponse response = new DepositResponse();
        response.setId(deposit.getId());
        response.setMemberId(deposit.getMember().getId());
        response.setMemberName(deposit.getMember().getFirstName() + " " + deposit.getMember().getLastName());
        response.setAmount(deposit.getAmount());
        response.setDepositDate(deposit.getDepositDate());
        response.setStatus(deposit.getStatus());
        response.setInterestEarned(deposit.getInterestEarned());
        response.setTotalAmount(deposit.getTotalAmount());
        response.setReturnDate(deposit.getReturnDate());
        response.setInterestRate(deposit.getInterestRate());
        InterestCalculator.DurationResult duration = InterestCalculator.calculateDuration(deposit.getDepositDate(), deposit.getReturnDate() != null ? deposit.getReturnDate() : LocalDate.now());
        response.setDurationDays(duration.days);
        response.setDurationMonths(duration.months);
        return response;
    }
}
