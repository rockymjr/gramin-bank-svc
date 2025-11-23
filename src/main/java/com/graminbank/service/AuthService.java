package com.graminbank.service;

import com.graminbank.dto.request.LoginRequest;
import com.graminbank.dto.response.AuthResponse;
import com.graminbank.exception.AuthenticationException;
import com.graminbank.model.AdminUser;
import com.graminbank.repository.AdminUserRepository;
import com.graminbank.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse authenticate(LoginRequest request) {
        AdminUser admin = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(admin.getUsername());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(admin.getUsername());
        response.setExpiresIn(86400L); // 24 hours in seconds

        return response;
    }
}