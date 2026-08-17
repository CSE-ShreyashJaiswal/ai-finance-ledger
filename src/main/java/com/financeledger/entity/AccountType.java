package com.financeledger.entity;

/**
 * Account classification in a double-entry ledger.
 *
 * <ul>
 *   <li><b>ASSET</b> — something you own (bank account, cash, investments).</li>
 *   <li><b>LIABILITY</b> — something you owe (credit card balance, loan).</li>
 * </ul>
 *
 * In double-entry accounting:
 * <ul>
 *   <li>Debiting an ASSET account <em>increases</em> its balance.</li>
 *   <li>Crediting an ASSET account <em>decreases</em> its balance.</li>
 *   <li>Debiting a LIABILITY account <em>decreases</em> its balance.</li>
 *   <li>Crediting a LIABILITY account <em>increases</em> its balance.</li>
 * </ul>
 */
public enum AccountType {
    ASSET,
    LIABILITY
}
