package com.financeledger.repository;

import com.financeledger.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for idempotency key operations.
 *
 * <p>The critical method is {@link #tryClaimKey} which uses {@code INSERT ... ON CONFLICT}
 * to atomically claim a key without race conditions.
 */
@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    /**
     * Atomically attempt to claim an idempotency key.
     *
     * <p>Uses {@code INSERT ... ON CONFLICT DO NOTHING} so that:
     * <ul>
     *   <li>If the key is new → row is inserted, returns 1 (claimed).</li>
     *   <li>If the key exists → nothing happens, returns 0 (already claimed).</li>
     * </ul>
     *
     * <p>This MUST run inside a {@code REQUIRES_NEW} transaction to isolate the
     * claim from the outer business transaction (see Section 5.2 of the plan).
     *
     * @return number of rows inserted (1 = claimed, 0 = already exists)
     */
    @Modifying
    @Query(value = "INSERT INTO idempotency_records (idempotency_key, user_id, status, created_at, updated_at) " +
                   "VALUES (:key, :userId, 'PROCESSING', NOW(), NOW()) " +
                   "ON CONFLICT (idempotency_key) DO NOTHING",
           nativeQuery = true)
    int tryClaimKey(@Param("key") String key, @Param("userId") Long userId);

    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}
