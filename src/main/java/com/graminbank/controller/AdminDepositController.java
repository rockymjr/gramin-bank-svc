package com.graminbank.controller;

import com.graminbank.dto.request.DepositRequest;
import com.graminbank.dto.response.DepositResponse;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/deposits")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDepositController {

    private final DepositService depositService;

    @PostMapping
    public ResponseEntity<DepositResponse> createDeposit(@Valid @RequestBody DepositRequest request) {
        DepositResponse response = depositService.createDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<DepositResponse>> getDeposits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DepositResponse> deposits = depositService.getActiveDeposits(
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(deposits);
    }
}