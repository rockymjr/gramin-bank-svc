package com.graminbank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePinRequest {
    @NotBlank(message = "Old PIN is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "Old PIN must be 4 digits")
    private String oldPin;

    @NotBlank(message = "New PIN is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "New PIN must be 4 digits")
    private String newPin;
}