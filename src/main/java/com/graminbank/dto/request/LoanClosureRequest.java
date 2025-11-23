package com.graminbank.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanClosureRequest {

    @NotNull(message = "Return date is required")
    private LocalDate returnDate;
}
