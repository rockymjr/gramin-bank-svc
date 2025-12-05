package com.graminbank.service;

import com.graminbank.model.Member;
import com.graminbank.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Separate service to handle login attempts in independent transactions.
 * This ensures that failed login attempts are persisted even when
 * authentication fails and throws an exception.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final MemberRepository memberRepository;

    /**
     * Records a failed login attempt in a NEW transaction.
     * This transaction will commit even if the calling method's transaction rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.incrementFailedAttempts();
        memberRepository.saveAndFlush(member); // Force immediate database write

        log.warn("Failed login attempt recorded for member: {} (Phone: {}) - Total attempts: {}/3",
                member.getId(), member.getPhone(), member.getFailedLoginAttempts());

        if (member.isCurrentlyBlocked()) {
            log.warn("Member {} (Phone: {}) has been BLOCKED until {}",
                    member.getId(), member.getPhone(), member.getBlockedUntil());
        }
    }

    /**
     * Records a successful login in a NEW transaction.
     * This resets the failed login attempt counter.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccessfulLogin(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        int previousAttempts = member.getFailedLoginAttempts();
        member.resetFailedAttempts();
        memberRepository.saveAndFlush(member); // Force immediate database write

        if (previousAttempts > 0) {
            log.info("Successful login recorded for member: {} (Phone: {}) - Failed attempts reset from {} to 0",
                    member.getId(), member.getPhone(), previousAttempts);
        } else {
            log.info("Successful login recorded for member: {} (Phone: {})",
                    member.getId(), member.getPhone());
        }
    }

    /**
     * Gets the current member state from database.
     * Used to refresh member data after transaction commits.
     */
    @Transactional(readOnly = true)
    public Member getMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }
}