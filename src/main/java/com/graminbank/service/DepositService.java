package com.graminbank.service;


import com.graminbank.dto.request.DepositRequest;
import com.graminbank.dto.response.DepositResponse;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.model.Deposit;
import com.graminbank.model.Member;
import com.graminbank.repository.DepositRepository;
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
public class DepositService {

    private final DepositRepository depositRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public DepositResponse createDeposit(DepositRequest request) {
        log.info("Creating deposit for member: {}", request.getMemberId());

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Deposit deposit = new Deposit();
        deposit.setMember(member);
        deposit.setAmount(request.getAmount());
        deposit.setDepositDate(request.getDepositDate());
        deposit.setInterestRate(new BigDecimal("2.5"));
        deposit.setFinancialYear(String.valueOf(LocalDate.now().getYear()));
        deposit.setStatus("ACTIVE");
        Deposit savedDeposit = depositRepository.save(deposit);
        return convertToResponse(savedDeposit);
    }

    public Page<DepositResponse> getActiveDeposits(Pageable pageable) {
        return depositRepository.findByStatus("ACTIVE", pageable)
                .map(this::convertToResponse);
    }

    public List<Deposit> getDepositsByMember(UUID memberId) {
        return depositRepository.findByMemberId(memberId);
    }

    @Transactional
    public void settleDeposit(Deposit deposit, LocalDate settlementDate) {
        BigDecimal interest = InterestCalculator.calculateDepositInterest(
                deposit.getAmount(),
                deposit.getDepositDate(),
                settlementDate
        );

        deposit.setInterestEarned(interest);
        deposit.setTotalAmount(deposit.getAmount().add(interest));
        deposit.setStatus("SETTLED");
        deposit.setSettledDate(settlementDate);

        depositRepository.save(deposit);
    }

    private DepositResponse convertToResponse(Deposit deposit) {
        DepositResponse response = new DepositResponse();
        response.setId(deposit.getId());
        response.setMemberId(deposit.getMember().getId());
        response.setMemberName(deposit.getMember().getFirstName() + " " + deposit.getMember().getLastName());
        response.setAmount(deposit.getAmount());
        response.setDepositDate(deposit.getDepositDate());
        response.setStatus(deposit.getStatus());
        response.setInterestEarned(deposit.getInterestEarned());
        response.setTotalAmount(deposit.getTotalAmount());
        return response;
    }
}