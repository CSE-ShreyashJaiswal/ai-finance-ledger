package com.financeledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a new account.
 *
 * <p>Note: {@code userId} is a temporary field used until JWT auth is added in Week 3.
 * After that, the user will be resolved from the authentication principal.
 */
public record CreateAccountRequest(
        @NotNull(message = "userId is required")
        Long userId,                    // TODO: Remove in Week 3 — get from JWT instead

        @NotBlank(message = "Account name is required")
        String name,

        @NotBlank(message = "Account type is required (ASSET or LIABILITY)")
        String type
) {
}
