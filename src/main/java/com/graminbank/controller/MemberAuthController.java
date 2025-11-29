package com.graminbank.controller;

import com.graminbank.dto.request.ChangePinRequest;
import com.graminbank.dto.request.MemberLoginRequest;
import com.graminbank.dto.response.MemberAuthResponse;
import com.graminbank.dto.response.MemberDashboardResponse;
import com.graminbank.service.MemberAuthService;
import com.graminbank.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemberAuthController {

    private final MemberAuthService memberAuthService;
    private final MemberService memberService;

    @PostMapping("/auth/login")
    public ResponseEntity<MemberAuthResponse> login(@Valid @RequestBody MemberLoginRequest request) {
        MemberAuthResponse response = memberAuthService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<MemberDashboardResponse> getDashboard(Authentication authentication) {
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
        MemberDashboardResponse response = memberAuthService.getMemberDashboard(memberId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-pin")
    public ResponseEntity<Void> changePin(
            Authentication authentication,
            @Valid @RequestBody ChangePinRequest request) {
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
        memberService.changePin(memberId, request);
        return ResponseEntity.ok().build();
    }
}