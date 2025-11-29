package com.graminbank.repository;

import com.graminbank.model.Deposit;
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
public interface DepositRepository extends JpaRepository<Deposit, UUID> {

    List<Deposit> findByStatusAndFinancialYear(String status, String financialYear);

    List<Deposit> findByMemberId(UUID memberId);

    Page<Deposit> findByStatus(String status, Pageable pageable);

    List<Deposit> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.status = :status")
    BigDecimal getTotalDepositsByStatus(@Param("status") String status);

    @Query("SELECT COUNT(d) FROM Deposit d WHERE d.status = :status")
    Long countByStatus(@Param("status") String status);

    List<Deposit> findByMemberIdOrderByDepositDateDesc(UUID memberId);
    Page<Deposit> findByStatusOrderByDepositDateDesc(String status, Pageable pageable);
}