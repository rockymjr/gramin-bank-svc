package com.graminbank.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MemberResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String aadharNumber;
    private LocalDate joiningDate;
    private Boolean isActive;
}