package com.financeledger.controller;

import com.financeledger.dto.AccountResponse;
import com.financeledger.dto.BalanceResponse;
import com.financeledger.dto.CreateAccountRequest;
import com.financeledger.entity.Account;
import com.financeledger.entity.User;
import com.financeledger.security.CustomUserDetailsService;
import com.financeledger.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for account management and balance queries.
 *
 * <p>All endpoints require JWT authentication. The user is resolved from the token.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final CustomUserDetailsService userDetailsService;

    public AccountController(AccountService accountService,
                             CustomUserDetailsService userDetailsService) {
        this.accountService = accountService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication) {
        User user = userDetailsService.findUserByEmail(authentication.getName());
        Account account = accountService.createAccount(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(Authentication authentication) {
        User user = userDetailsService.findUserByEmail(authentication.getName());
        List<AccountResponse> accounts = accountService.getAccountsByUser(user.getId()).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(accounts);
    }

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
