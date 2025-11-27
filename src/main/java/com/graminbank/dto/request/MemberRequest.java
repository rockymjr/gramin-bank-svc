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

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits",
            groups = ValidationGroups.PhoneValidation.class)
    private String phone;  // Optional, no validation if empty

    private String address;

    public interface ValidationGroups {
        interface PhoneValidation {}
    }
}