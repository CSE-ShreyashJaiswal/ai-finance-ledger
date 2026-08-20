package com.financeledger.controller;

import com.financeledger.dto.AccountResponse;
import com.financeledger.dto.BalanceResponse;
import com.financeledger.dto.CreateAccountRequest;
import com.financeledger.entity.Account;
import com.financeledger.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for account management and balance queries.
 *
 * <p>Note: {@code userId} is passed as a request parameter until JWT auth
 * is added in Week 3, after which it will be resolved from the token.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Create a new account.
     *
     * <pre>POST /api/accounts</pre>
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    /**
     * List all accounts for a user.
     *
     * <pre>GET /api/accounts?userId=1</pre>
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(@RequestParam Long userId) {
        List<AccountResponse> accounts = accountService.getAccountsByUser(userId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get the computed balance for an account.
     *
     * <p>Balance is derived from the sum of ledger entries — never stored.
     *
     * <pre>GET /api/accounts/{id}/balance</pre>
     */
    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.computeBalance(id));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType().name(),
                account.getCreatedAt()
        );
    }
}
