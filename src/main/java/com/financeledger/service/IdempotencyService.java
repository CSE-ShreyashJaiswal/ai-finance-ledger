package com.financeledger.service;

import com.financeledger.entity.IdempotencyRecord;
import com.financeledger.entity.IdempotencyStatus;
import com.financeledger.repository.IdempotencyRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Idempotency service using the {@code INSERT ... ON CONFLICT} pattern
 * in a {@code REQUIRES_NEW} transaction to atomically claim keys.
 *
 * <p><b>Why REQUIRES_NEW?</b> The key claim must commit independently of the
 * outer business transaction. If two concurrent requests with the same key
 * arrive simultaneously:
 * <ol>
 *   <li>Thread A claims the key (INSERT succeeds, commits immediately).</li>
 *   <li>Thread B tries to claim the same key (INSERT conflicts, returns 0).</li>
 *   <li>Thread B reads the existing record and sees PROCESSING → returns 409.</li>
 * </ol>
 *
 * <p>Without REQUIRES_NEW, both threads might be in the same logical transaction,
 * and the conflict wouldn't be visible until commit time — creating a race condition.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    /**
     * Attempt to claim an idempotency key.
     *
     * @return empty if the key was successfully claimed (new key),
     *         or the existing record if the key was already claimed
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<IdempotencyRecord> claimKey(String key, Long userId) {
        int inserted = repository.tryClaimKey(key, userId);

        if (inserted == 1) {
            // Key successfully claimed — new key
            log.debug("Idempotency key claimed: {}", key);
            return Optional.empty();
        }

        // Key already exists — return the existing record
        log.debug("Idempotency key already exists: {}", key);
        return repository.findByIdempotencyKey(key);
    }

    /**
     * Mark a claimed key as COMPLETED with the cached response.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String key, Long transactionId, String responseBody, int responseStatus) {
        IdempotencyRecord record = repository.findByIdempotencyKey(key)
                .orElseThrow(() -> new IllegalStateException("Idempotency record not found: " + key));

        record.setStatus(IdempotencyStatus.COMPLETED);
        record.setTransactionId(transactionId);
        record.setResponseBody(responseBody);
        record.setResponseStatus(responseStatus);
        repository.save(record);

        log.debug("Idempotency key completed: {} → transaction {}", key, transactionId);
    }

    /**
     * Mark a claimed key as FAILED (client should retry with a new key).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String key) {
        repository.findByIdempotencyKey(key).ifPresent(record -> {
            record.setStatus(IdempotencyStatus.FAILED);
            repository.save(record);
            log.warn("Idempotency key failed: {}", key);
        });
    }
}
