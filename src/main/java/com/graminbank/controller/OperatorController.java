package com.graminbank.controller;

import com.graminbank.dto.response.MemberResponse;
import com.graminbank.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operator")
@PreAuthorize("hasRole('OPERATOR')")
@RequiredArgsConstructor
public class OperatorController {
    // Read-only endpoints for deposits, loans, members
    // Similar to admin but no POST/PUT/DELETE

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers(
            @RequestParam(required = false) String search) {
        List<MemberResponse> members = search != null ?
                memberService.searchMembers(search) :
                memberService.getAllActiveMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable UUID id) {
        MemberResponse response = memberService.getMemberById(id);
        return ResponseEntity.ok(response);
    }

}