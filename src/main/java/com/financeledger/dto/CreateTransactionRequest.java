package com.financeledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for creating a new transaction with balanced double-entry ledger entries.
 *
 * <p>The {@code amount} represents the value being moved. The system creates two
 * ledger entries: a DEBIT on {@code debitAccountId} and a CREDIT on {@code creditAccountId}.
 *
 * <p>Example: "Bought groceries for ₹500 from Bank account (ASSET) creating an Expense"
 * <pre>
 * {
 *   "description": "Groceries at Whole Foods",
 *   "amount": 500.00,
 *   "debitAccountId": 2,      // Expense account (ASSET — increases)
 *   "creditAccountId": 1,     // Bank account (ASSET — decreases)
 *   "category": "FOOD"
 * }
 * </pre>
 *
 * <p>Note: {@code userId} is temporary until JWT auth is added in Week 3.
 */
public record CreateTransactionRequest(
        @NotNull(message = "userId is required")
        Long userId,                    // TODO: Remove in Week 3 — get from JWT instead

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
