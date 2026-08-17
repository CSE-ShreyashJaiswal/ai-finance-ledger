package com.financeledger.repository;

import com.financeledger.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Account} entity operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts belonging to a specific user.
     */
    List<Account> findByUserId(Long userId);

    /**
     * Find an account by ID with a pessimistic write lock.
     *
     * <p>Used inside {@code @Transactional(isolation = REPEATABLE_READ)} boundaries
     * to prevent concurrent modifications to the same account during
     * transaction creation (see Section 5.1 of the implementation plan).
     *
     * <p>This is the standard pattern for financial systems: lock the specific
     * account rows rather than using SERIALIZABLE isolation (which requires
     * retry logic for 40001 serialization failures).
     */
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
