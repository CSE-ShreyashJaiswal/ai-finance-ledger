package com.financeledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for creating a new transaction with balanced double-entry ledger entries.
 *
 * <p>The user is resolved from the JWT authentication principal.
 * The optional {@code idempotencyKey} is sent via the {@code Idempotency-Key} header (not in the body).
 */
public record CreateTransactionRequest(
        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Debit account ID is required")
        Long debitAccountId,

        @NotNull(message = "Credit account ID is required")
        Long creditAccountId,

        String category                 // Optional — auto-categorized by AI in Week 5
) {
}
