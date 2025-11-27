package com.graminbank.repository;

import com.graminbank.model.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, UUID> {

    List<LoanPayment> findByLoanIdOrderByPaymentDateDesc(UUID loanId);

    List<LoanPayment> findByLoanId(UUID loanId);
}