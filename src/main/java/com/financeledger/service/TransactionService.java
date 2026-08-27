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
 *   <li>Uses {@code REPEATABLE_READ} isolation with pessimistic locking on accounts.</li>
 *   <li>Accounts are locked in ID order to prevent deadlocks.</li>
 *   <li>Idempotency is handled at the controller level via {@link IdempotencyService}.</li>
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
     * @param request the transaction details
     * @param userId  the authenticated user's ID (from JWT)
     * @return the created transaction
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Transaction createTransaction(CreateTransactionRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Lock accounts in ID order to prevent deadlocks
        Long firstId = Math.min(request.debitAccountId(), request.creditAccountId());
        Long secondId = Math.max(request.debitAccountId(), request.creditAccountId());

        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", firstId));
        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", secondId));

        Account debitAccount = request.debitAccountId().equals(firstId) ? firstAccount : secondAccount;
        Account creditAccount = request.creditAccountId().equals(firstId) ? firstAccount : secondAccount;

        // Validate ownership
        if (!debitAccount.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Debit account does not belong to user");
        }
        if (!creditAccount.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Credit account does not belong to user");
        }

        // Validate distinct accounts
        if (debitAccount.getId().equals(creditAccount.getId())) {
            throw new IllegalArgumentException("Debit and credit accounts must be different");
        }

        // Create transaction + balanced ledger entries
        Transaction transaction = new Transaction(user, request.description(), request.amount(), request.category());

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
     * List all transactions for the authenticated user, most recent first.
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
