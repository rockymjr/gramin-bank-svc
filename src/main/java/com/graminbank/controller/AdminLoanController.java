package com.graminbank.controller;

import com.graminbank.dto.request.LoanRequest;
import com.graminbank.dto.request.LoanClosureRequest;
import com.graminbank.dto.response.LoanResponse;
import com.graminbank.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest request) {
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<LoanResponse>> getLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LoanResponse> loans = loanService.getActiveLoans(
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(loans);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<LoanResponse> closeLoan(
            @PathVariable UUID id,
            @Valid @RequestBody LoanClosureRequest request) {
        LoanResponse response = loanService.closeLoan(id, request.getReturnDate());
        return ResponseEntity.ok(response);
    }
}
