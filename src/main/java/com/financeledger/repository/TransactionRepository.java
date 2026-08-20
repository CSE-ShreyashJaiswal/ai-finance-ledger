package com.financeledger.repository;

import com.financeledger.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Transaction} entity operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions belonging to a specific user, ordered by most recent first.
     */
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
