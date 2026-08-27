package com.financeledger.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}
