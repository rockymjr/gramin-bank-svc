package com.graminbank.security;

import com.graminbank.model.Member;
import com.graminbank.repository.MemberRepository;
import com.graminbank.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, username)) {
                List<SimpleGrantedAuthority> authorities;

                // Check if it's a member token (starts with MEMBER_)
                if (username.startsWith("MEMBER_")) {
                    // Extract member ID and check if operator
                    UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
                    Member member = memberRepository.findById(memberId).orElse(null);

                    if (member != null && member.getIsOperator()) {
                        authorities = Arrays.asList(
                                new SimpleGrantedAuthority("ROLE_OPERATOR"),
                                new SimpleGrantedAuthority("ROLE_MEMBER")
                        );
                    } else {
                        authorities = Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_MEMBER")
                        );
                    }
                } else {
                    // Admin token
                    authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_ADMIN")
                    );
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}