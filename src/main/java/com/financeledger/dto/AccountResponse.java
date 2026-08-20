package com.financeledger.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for account details.
 */
public record AccountResponse(
        Long id,
        String name,
        String type,
        LocalDateTime createdAt
) {
}
