package com.financeledger.repository;

import com.financeledger.entity.EntryType;
import com.financeledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Repository for {@link LedgerEntry} entity operations.
 *
 * <p>The key method here is {@link #sumAmountByAccountIdAndEntryType} —
 * it's used to compute account balances from the ledger entries
 * rather than storing a balance column (which would risk drift).
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    /**
     * Sum all entry amounts for a given account and entry type (DEBIT or CREDIT).
     *
     * <p>Used by {@code AccountService.computeBalance()} to derive the account
     * balance from the ledger — the core principle of double-entry accounting.
     *
     * @return the sum, or {@code BigDecimal.ZERO} if no entries exist (via COALESCE)
     */
    @Query("SELECT COALESCE(SUM(le.amount), 0) FROM LedgerEntry le " +
           "WHERE le.account.id = :accountId AND le.entryType = :entryType")
    BigDecimal sumAmountByAccountIdAndEntryType(
            @Param("accountId") Long accountId,
            @Param("entryType") EntryType entryType);
}
