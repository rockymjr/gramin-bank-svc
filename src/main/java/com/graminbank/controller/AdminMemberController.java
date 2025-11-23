package com.graminbank.controller;

import com.graminbank.dto.request.MemberRequest;
import com.graminbank.dto.response.MemberResponse;
import com.graminbank.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable UUID id,
            @Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.updateMember(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateMember(@PathVariable UUID id) {
        memberService.deactivateMember(id);
        return ResponseEntity.noContent().build();
    }
}