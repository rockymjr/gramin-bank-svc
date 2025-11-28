package com.graminbank.dto.response;


import lombok.Data;
import java.util.UUID;

@Data
public class MemberAuthResponse {
    private String token;
    private UUID memberId;
    private String memberName;
    private String phone;
    private Long expiresIn;
}