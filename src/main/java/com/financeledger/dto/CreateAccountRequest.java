package com.financeledger.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating a new account.
 *
 * <p>The user is resolved from the JWT authentication principal.
 */
public record CreateAccountRequest(
        @NotBlank(message = "Account name is required")
        String name,

        @NotBlank(message = "Account type is required (ASSET or LIABILITY)")
        String type
) {
}
