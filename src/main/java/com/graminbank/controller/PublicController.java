package com.graminbank.controller;

import com.graminbank.dto.response.MaskedDepositResponse;
import com.graminbank.dto.response.MaskedLoanResponse;
import com.graminbank.dto.response.SummaryResponse;
import com.graminbank.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicController {

    private final PublicService publicService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary() {
        return ResponseEntity.ok(publicService.getSummary());
    }

    @GetMapping("/deposits")
    public ResponseEntity<Page<MaskedDepositResponse>> getDeposits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MaskedDepositResponse> deposits = publicService.getMaskedDeposits(
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(deposits);
    }

    @GetMapping("/loans")
    public ResponseEntity<Page<MaskedLoanResponse>> getLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MaskedLoanResponse> loans = publicService.getMaskedLoans(
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(loans);
    }
}