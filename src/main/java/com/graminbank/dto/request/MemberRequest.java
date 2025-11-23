package com.graminbank.dto.request;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MemberRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private String address;

    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}-[0-9]{4}$", message = "Invalid Aadhar format")
    private String aadharNumber;
}