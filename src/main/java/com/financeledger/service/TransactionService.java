package com.financeledger.service;

import com.financeledger.dto.CreateTransactionRequest;
import com.financeledger.dto.TransactionResponse;
import com.financeledger.entity.Account;
import com.financeledger.entity.EntryType;
import com.financeledger.entity.LedgerEntry;
import com.financeledger.entity.Transaction;
import com.financeledger.entity.User;
import com.financeledger.exception.ResourceNotFoundException;
import com.financeledger.repository.AccountRepository;
import com.financeledger.repository.TransactionRepository;
import com.financeledger.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core transaction service implementing double-entry ledger logic.
 *
 * <p><b>The most interview-relevant class in the project.</b>
 *
 * <p>Key design decisions:
 * <ul>
 *   <li>Every transaction creates exactly two balanced ledger entries (DEBIT + CREDIT).</li>
 *   <li>Uses {@code REPEATABLE_READ} isolation with pessimistic locking ({@code SELECT FOR UPDATE})
 *       on the affected accounts to prevent lost updates under concurrent requests.</li>
 *   <li>Pessimistic locking chosen over {@code SERIALIZABLE} to avoid serialization-failure
 *       retry complexity ({@code 40001} errors in Postgres SSI).</li>
 * </ul>
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a transaction with balanced double-entry ledger entries.
     *
     * <p>This method:
     * <ol>
     *   <li>Acquires pessimistic write locks on both accounts (prevents concurrent modification)</li>
     *   <li>Validates ownership and distinct accounts</li>
     *   <li>Creates a {@link Transaction} with two {@link LedgerEntry} records</li>
     *   <li>The entries balance to zero: one DEBIT, one CREDIT, same amount</li>
     * </ol>
     *
     * <p>The {@code REPEATABLE_READ} isolation + {@code PESSIMISTIC_WRITE} lock ensures
     * correctness under concurrent requests without the retry complexity of {@code SERIALIZABLE}.
     *
     * @param request the transaction details (accounts, amount, description)
     * @return the created transaction with its ledger entries
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Transaction createTransaction(CreateTransactionRequest request) {

        // 1. Resolve the user
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        // 2. Lock both accounts with SELECT FOR UPDATE (pessimistic write lock)
        //    Order by ID to prevent deadlocks when two transactions lock the same accounts in reverse order.
        Long firstId = Math.min(request.debitAccountId(), request.creditAccountId());
        Long secondId = Math.max(request.debitAccountId(), request.creditAccountId());

        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", firstId));
        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", secondId));

        // Map back to debit/credit after ordered locking
        Account debitAccount = request.debitAccountId().equals(firstId) ? firstAccount : secondAccount;
        Account creditAccount = request.creditAccountId().equals(firstId) ? firstAccount : secondAccount;

        // 3. Validate: both accounts must belong to the same user
        if (!debitAccount.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Debit account does not belong to user");
        }
        if (!creditAccount.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Credit account does not belong to user");
        }

        // 4. Validate: debit and credit accounts must be different
        if (debitAccount.getId().equals(creditAccount.getId())) {
            throw new IllegalArgumentException("Debit and credit accounts must be different");
        }

        // 5. Create the transaction
        Transaction transaction = new Transaction(user, request.description(), request.amount(), request.category());

        // 6. Create balanced ledger entries (DEBIT + CREDIT, same amount)
        LedgerEntry debitEntry = new LedgerEntry(transaction, debitAccount, EntryType.DEBIT, request.amount());
        LedgerEntry creditEntry = new LedgerEntry(transaction, creditAccount, EntryType.CREDIT, request.amount());

        transaction.addLedgerEntry(debitEntry);
        transaction.addLedgerEntry(creditEntry);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Created transaction id={} amount={} debit={} credit={}",
                saved.getId(), saved.getAmount(), debitAccount.getName(), creditAccount.getName());

        return saved;
    }

    /**
     * List all transactions for a user, most recent first.
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Convert a Transaction entity to its API response DTO.
     */
    public TransactionResponse toResponse(Transaction transaction) {
        List<TransactionResponse.LedgerEntryResponse> entries = transaction.getLedgerEntries().stream()
                .map(entry -> new TransactionResponse.LedgerEntryResponse(
                        entry.getId(),
                        entry.getAccount().getId(),
                        entry.getAccount().getName(),
                        entry.getEntryType().name(),
                        entry.getAmount()
                ))
                .toList();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getCreatedAt(),
                entries
        );
    }
}
