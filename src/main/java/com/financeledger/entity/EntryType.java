package com.financeledger.entity;

/**
 * Ledger entry direction in a double-entry accounting system.
 *
 * <p>Every transaction produces at least two entries — one DEBIT and one CREDIT —
 * whose amounts balance to zero.
 *
 * <p>Balance computation depends on account type:
 * <ul>
 *   <li><b>ASSET</b> accounts: balance = Σ(DEBIT) − Σ(CREDIT)</li>
 *   <li><b>LIABILITY</b> accounts: balance = Σ(CREDIT) − Σ(DEBIT)</li>
 * </ul>
 */
public enum EntryType {
    DEBIT,
    CREDIT
}
