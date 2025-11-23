package com.graminbank.repository;


import com.graminbank.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    List<Member> findByIsActiveTrue();

    @Query("SELECT m FROM Member m WHERE " +
            "LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "m.phone LIKE CONCAT('%', :search, '%')")
    List<Member> searchMembers(@Param("search") String search);

    boolean existsByPhone(String phone);
}