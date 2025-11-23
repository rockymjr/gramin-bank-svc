package com.graminbank.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String username;
    private Long expiresIn;
}
