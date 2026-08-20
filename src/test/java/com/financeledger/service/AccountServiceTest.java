package com.financeledger.service;

import com.financeledger.dto.BalanceResponse;
import com.financeledger.entity.Account;
import com.financeledger.entity.AccountType;
import com.financeledger.entity.EntryType;
import com.financeledger.entity.User;
import com.financeledger.exception.ResourceNotFoundException;
import com.financeledger.repository.AccountRepository;
import com.financeledger.repository.LedgerEntryRepository;
import com.financeledger.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ledger balance computation logic.
 *
 * <p>These tests verify the core double-entry accounting math:
 * <ul>
 *   <li>ASSET balance = Σ(DEBIT) − Σ(CREDIT)</li>
 *   <li>LIABILITY balance = Σ(CREDIT) − Σ(DEBIT)</li>
 *   <li>Edge cases: no entries, account not found</li>
 * </ul>
 *
 * <p>This is the test class interviewers are most likely to examine.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private Account assetAccount;
    private Account liabilityAccount;

    @BeforeEach
    void setUp() {
        User user = new User("test@example.com", "hashed");

        assetAccount = new Account(user, "Bank Savings", AccountType.ASSET);
        liabilityAccount = new Account(user, "Credit Card", AccountType.LIABILITY);
    }

    @Nested
    @DisplayName("ASSET account balance computation")
    class AssetBalanceTests {

        @Test
        @DisplayName("Balance = debits - credits for ASSET accounts")
        void assetBalance_debitsMinusCredits() {
            // Given: ASSET account with ₹1000 in debits and ₹300 in credits
            when(accountRepository.findById(1L)).thenReturn(Optional.of(assetAccount));
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(1L, EntryType.DEBIT))
                    .thenReturn(new BigDecimal("1000.0000"));
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(1L, EntryType.CREDIT))
                    .thenReturn(new BigDecimal("300.0000"));

            // When
            BalanceResponse result = accountService.computeBalance(1L);

            // Then: balance = 1000 - 300 = 700
            assertEquals(0, new BigDecimal("700.0000").compareTo(result.balance()));
            assertEquals("ASSET", result.accountType());
        }

        @Test
        @DisplayName("Negative balance when credits exceed debits")
        void assetBalance_negativeWhenCreditsExceedDebits() {
            // Given: more credits than debits (e.g., overdrawn account)
            when(accountRepository.findById(1L)).thenReturn(Optional.of(assetAccount));
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(1L, EntryType.DEBIT))
                    .thenReturn(new BigDecimal("200.0000"));
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(1L, EntryType.CREDIT))
                    .thenReturn(new BigDecimal("500.0000"));

            // When
            BalanceResponse result = accountService.computeBalance(1L);

            // Then: balance = 200 - 500 = -300
            assertEquals(0, new BigDecimal("-300.0000").compareTo(result.balance()));
        }
    }

    @Nested
    @DisplayName("LIABILITY account balance computation")
    class LiabilityBalanceTests {

        @Test
        @DisplayName("Balance = credits - debits for LIABILITY accounts")
        void liabilityBalance_creditsMinusDebits() {
            // Given: LIABILITY account with ₹800 in credits and ₹200 in debits
            when(accountRepository.findById(2L)).thenReturn(Optional.of(liabilityAccount));
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(2L, EntryType.DEBIT))
                    .thenReturn(new BigDecimal("200.0000"));
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(2L, EntryType.CREDIT))
                    .thenReturn(new BigDecimal("800.0000"));

            // When
            BalanceResponse result = accountService.computeBalance(2L);

            // Then: balance = 800 - 200 = 600
            assertEquals(0, new BigDecimal("600.0000").compareTo(result.balance()));
            assertEquals("LIABILITY", result.accountType());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Zero balance when no ledger entries exist")
        void zeroBalance_whenNoEntries() {
            // Given: account with no transactions at all
            when(accountRepository.findById(1L)).thenReturn(Optional.of(assetAccount));
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(1L, EntryType.DEBIT))
                    .thenReturn(BigDecimal.ZERO);
            when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(1L, EntryType.CREDIT))
                    .thenReturn(BigDecimal.ZERO);

            // When
            BalanceResponse result = accountService.computeBalance(1L);

            // Then
            assertEquals(0, BigDecimal.ZERO.compareTo(result.balance()));
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException for nonexistent account")
        void throwsNotFound_whenAccountDoesNotExist() {
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> accountService.computeBalance(999L));
        }
    }
}
