package com.financeledger.service;

import com.financeledger.dto.BalanceResponse;
import com.financeledger.dto.CreateAccountRequest;
import com.financeledger.entity.Account;
import com.financeledger.entity.AccountType;
import com.financeledger.entity.EntryType;
import com.financeledger.entity.User;
import com.financeledger.exception.ResourceNotFoundException;
import com.financeledger.repository.AccountRepository;
import com.financeledger.repository.LedgerEntryRepository;
import com.financeledger.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for account operations including balance computation.
 *
 * <p><b>Key design principle:</b> Account balance is <em>never</em> stored as a column.
 * It is always computed from the sum of ledger entries. This eliminates drift
 * between a cached balance and the source of truth — exactly what a real
 * financial system does.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository,
                          LedgerEntryRepository ledgerEntryRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new account for a user.
     */
    @Transactional
    public Account createAccount(CreateAccountRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        AccountType type;
        try {
            type = AccountType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid account type: " + request.type()
                    + ". Must be ASSET or LIABILITY.");
        }

        Account account = new Account(user, request.name(), type);
        return accountRepository.save(account);
    }

    /**
     * List all accounts for a given user.
     */
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return accountRepository.findByUserId(userId);
    }

    /**
     * Compute the balance for an account from its ledger entries.
     *
     * <p>Balance formula:
     * <ul>
     *   <li><b>ASSET</b> accounts: balance = Σ(DEBIT) − Σ(CREDIT)</li>
     *   <li><b>LIABILITY</b> accounts: balance = Σ(CREDIT) − Σ(DEBIT)</li>
     * </ul>
     *
     * <p>This is the heart of the double-entry ledger. No balance column exists;
     * the balance is always derived from the ledger entries.
     */
    @Transactional(readOnly = true)
    public BalanceResponse computeBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        BigDecimal totalDebits = ledgerEntryRepository
                .sumAmountByAccountIdAndEntryType(accountId, EntryType.DEBIT);
        BigDecimal totalCredits = ledgerEntryRepository
                .sumAmountByAccountIdAndEntryType(accountId, EntryType.CREDIT);

        BigDecimal balance;
        if (account.getType() == AccountType.ASSET) {
            // ASSET: debits increase, credits decrease
            balance = totalDebits.subtract(totalCredits);
        } else {
            // LIABILITY: credits increase, debits decrease
            balance = totalCredits.subtract(totalDebits);
        }

        return new BalanceResponse(
                account.getId(),
                account.getName(),
                account.getType().name(),
                balance
        );
    }
}
