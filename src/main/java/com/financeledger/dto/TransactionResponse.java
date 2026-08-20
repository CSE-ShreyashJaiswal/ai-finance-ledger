package com.financeledger.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a transaction including its ledger entries.
 */
public record TransactionResponse(
        Long id,
        String description,
        BigDecimal amount,
        String category,
        LocalDateTime createdAt,
        List<LedgerEntryResponse> ledgerEntries
) {

    /**
     * Nested DTO for a single ledger entry within a transaction response.
     */
    public record LedgerEntryResponse(
            Long id,
            Long accountId,
            String accountName,
            String entryType,
            BigDecimal amount
    ) {
    }
}
