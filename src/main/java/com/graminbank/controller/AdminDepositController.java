package com.graminbank.controller;

import com.graminbank.dto.request.DepositRequest;
import com.graminbank.dto.request.DepositReturnRequest;
import com.graminbank.dto.request.DepositUpdateRequest;
import com.graminbank.dto.response.DepositResponse;
import com.graminbank.service.DepositService;
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
@RequestMapping("/api/admin/deposits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class AdminDepositController {

    private final DepositService depositService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepositResponse> createDeposit(@Valid @RequestBody DepositRequest request) {
        DepositResponse response = depositService.createDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositResponse> getDepositById(@PathVariable UUID id) {
        DepositResponse response = depositService.getDepositById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepositResponse> updateDeposit(
            @PathVariable UUID id,
            @Valid @RequestBody DepositUpdateRequest request) {
        DepositResponse response = depositService.updateDeposit(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<DepositResponse>> getDeposits(
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DepositResponse> deposits = depositService.getDepositsByStatus(
                status, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(deposits);
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepositResponse> returnDeposit(
            @PathVariable UUID id,
            @Valid @RequestBody DepositReturnRequest request) {
        DepositResponse response = depositService.returnDeposit(id, request.getReturnDate());
        return ResponseEntity.ok(response);
    }
}