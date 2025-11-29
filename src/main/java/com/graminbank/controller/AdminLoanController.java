package com.graminbank.controller;

import com.graminbank.dto.request.LoanRequest;
import com.graminbank.dto.request.LoanClosureRequest;
import com.graminbank.dto.request.LoanUpdateRequest;
import com.graminbank.dto.request.LoanPaymentRequest;
import com.graminbank.dto.response.LoanResponse;
import com.graminbank.dto.response.LoanPaymentResponse;
import com.graminbank.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class AdminLoanController {

    private final LoanService loanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest request) {
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable UUID id) {
        LoanResponse response = loanService.getLoanById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> updateLoan(
            @PathVariable UUID id,
            @Valid @RequestBody LoanUpdateRequest request) {
        LoanResponse response = loanService.updateLoan(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<LoanResponse>> getLoans(
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LoanResponse> loans = loanService.getLoansByStatus(
                status, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanPaymentResponse> addPayment(
            @PathVariable UUID id,
            @Valid @RequestBody LoanPaymentRequest request) {
        LoanPaymentResponse response = loanService.addPayment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<LoanPaymentResponse>> getPaymentHistory(@PathVariable UUID id) {
        List<LoanPaymentResponse> payments = loanService.getPaymentHistory(id);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanResponse> closeLoan(
            @PathVariable UUID id,
            @Valid @RequestBody LoanClosureRequest request) {
        LoanResponse response = loanService.closeLoan(id, request);
        return ResponseEntity.ok(response);
    }
}