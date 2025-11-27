package com.graminbank.service;

import com.graminbank.dto.request.MemberRequest;
import com.graminbank.dto.response.MemberResponse;
import com.graminbank.exception.ResourceNotFoundException;
import com.graminbank.model.Member;
import com.graminbank.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        log.info("Creating new member: {} {}", request.getFirstName(), request.getLastName());

        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setPhone(request.getPhone());
        member.setAddress(request.getAddress());
        member.setJoiningDate(LocalDate.now());
        member.setIsActive(true);

        Member savedMember = memberRepository.save(member);
        return convertToResponse(savedMember);
    }

    public List<MemberResponse> getAllActiveMembers() {
        return memberRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<MemberResponse> searchMembers(String search) {
        return memberRepository.searchMembers(search)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public MemberResponse getMemberById(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return convertToResponse(member);
    }

    @Transactional
    public MemberResponse updateMember(UUID id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setPhone(request.getPhone());
        member.setAddress(request.getAddress());
        Member updatedMember = memberRepository.save(member);
        return convertToResponse(updatedMember);
    }

    @Transactional
    public void deactivateMember(UUID id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        member.setIsActive(false);
        memberRepository.save(member);
    }

    private MemberResponse convertToResponse(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setPhone(member.getPhone());
        response.setAddress(member.getAddress());
        response.setJoiningDate(member.getJoiningDate());
        response.setIsActive(member.getIsActive());
        return response;
    }
}