package com.graminbank.repository;

import com.graminbank.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByStatusAndFinancialYear(String status, String financialYear);

    List<Loan> findByMemberId(UUID memberId);

    Page<Loan> findByStatus(String status, Pageable pageable);

    List<Loan> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(l.loanAmount), 0) FROM Loan l WHERE l.status = :status")
    BigDecimal getTotalLoansByStatus(@Param("status") String status);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = :status")
    Long countByStatus(@Param("status") String status);
}