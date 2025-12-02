package com.graminbank.service;

import com.graminbank.dto.request.DepositRequest;
import com.graminbank.dto.request.DepositUpdateRequest;
import com.graminbank.dto.response.DepositResponse;
import com.graminbank.exception.BusinessException;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.model.Deposit;
import com.graminbank.model.Member;
import com.graminbank.repository.DepositRepository;
import com.graminbank.repository.MemberRepository;
import com.graminbank.util.InterestCalculator;
import com.graminbank.util.DepositMapper;
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

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Deposit amount must be greater than 0");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Deposit deposit = new Deposit();
        deposit.setMember(member);
        deposit.setAmount(request.getAmount());
        deposit.setDepositDate(request.getDepositDate());
        deposit.setInterestRate(request.getInterestRate());
        deposit.setNotes(request.getNotes());
        deposit.setFinancialYear(InterestCalculator.getFinancialYearFromDate(request.getDepositDate()));
        deposit.setStatus("ACTIVE");

        Deposit savedDeposit = depositRepository.save(deposit);
        return DepositMapper.convertToResponse(savedDeposit);
    }

    @Transactional
    public DepositResponse updateDeposit(UUID depositId, DepositUpdateRequest request) {
        log.info("Updating deposit: {}", depositId);

        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));

        if (!"ACTIVE".equals(deposit.getStatus())) {
            throw new BusinessException("Can only edit active deposits");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Deposit amount must be greater than 0");
        }

        // Update fields
        deposit.setAmount(request.getAmount());
        deposit.setDepositDate(request.getDepositDate());
        deposit.setFinancialYear(InterestCalculator.getFinancialYearFromDate(request.getDepositDate()));

        // Update member if changed
        if (request.getMemberId() != null && !request.getMemberId().equals(deposit.getMember().getId())) {
            Member newMember = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
            deposit.setMember(newMember);
        }

        Deposit updatedDeposit = depositRepository.save(deposit);
        return DepositMapper.convertToResponse(updatedDeposit);
    }

    public Page<DepositResponse> getDepositsByStatus(String status, Pageable pageable) {
        return   ( "ALL".equals(status) ? depositRepository.findAllByOrderByDepositDateDesc(pageable)
                : depositRepository.findByStatusOrderByDepositDateDesc(status, pageable))
                .map(DepositMapper::convertToResponseWithCurrentInterest);
    }

    public DepositResponse getDepositById(UUID depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));
        return DepositMapper.convertToResponseWithCurrentInterest(deposit);
    }

    public List<Deposit> getDepositsByMember(UUID memberId) {
        return depositRepository.findByMemberId(memberId);
    }

    @Transactional
    public DepositResponse returnDeposit(UUID depositId, LocalDate returnDate) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found"));

        if (!"ACTIVE".equals(deposit.getStatus())) {
            throw new IllegalStateException("Deposit is not active");
        }

        BigDecimal interest = InterestCalculator.calculateDepositInterest(
                deposit.getAmount(),
                deposit.getDepositDate(),
                returnDate
        );

        deposit.setInterestEarned(interest);
        deposit.setTotalAmount(deposit.getAmount().add(interest));
        deposit.setStatus("RETURNED");
        deposit.setReturnDate(returnDate);

        Deposit returned = depositRepository.save(deposit);
        return DepositMapper.convertToResponse(returned);
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
        deposit.setReturnDate(settlementDate);

        depositRepository.save(deposit);
    }

}