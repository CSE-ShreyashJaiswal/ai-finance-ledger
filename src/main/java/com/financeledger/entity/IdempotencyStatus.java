package com.financeledger.entity;

/**
 * Status of an idempotency key claim.
 *
 * <ul>
 *   <li><b>PROCESSING</b> — Key claimed, transaction creation in progress.</li>
 *   <li><b>COMPLETED</b> — Transaction created successfully. Response cached for replay.</li>
 *   <li><b>FAILED</b> — Transaction creation failed. Client should retry with a new key.</li>
 * </ul>
 */
public enum IdempotencyStatus {
    PROCESSING,
    COMPLETED,
    FAILED
}
