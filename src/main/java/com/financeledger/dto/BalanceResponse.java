package com.financeledger.dto;

import java.math.BigDecimal;

/**
 * Response DTO for computed account balance.
 *
 * <p>Balance is always computed from the sum of ledger entries — never stored.
 * <ul>
 *   <li>ASSET accounts: balance = Σ(DEBIT) − Σ(CREDIT)</li>
 *   <li>LIABILITY accounts: balance = Σ(CREDIT) − Σ(DEBIT)</li>
 * </ul>
 */
public record BalanceResponse(
        Long accountId,
        String accountName,
        String accountType,
        BigDecimal balance
) {
}
