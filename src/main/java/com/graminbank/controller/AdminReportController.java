package com.graminbank.controller;


import com.graminbank.dto.response.MemberStatementResponse;
import com.graminbank.dto.response.YearlySettlementResponse;
import com.graminbank.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/members/{memberId}/statement")
    public ResponseEntity<MemberStatementResponse> getMemberStatement(
            @PathVariable UUID memberId,
            @RequestParam(required = false) String year) {
        MemberStatementResponse response = reportService.getMemberStatement(memberId, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly-settlement")
    public ResponseEntity<YearlySettlementResponse> getYearlySettlement(
            @RequestParam(required = false) String year) {
        YearlySettlementResponse response = reportService.getYearlySettlement(year);
        return ResponseEntity.ok(response);
    }
}