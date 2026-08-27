package com.financeledger.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeledger.dto.CreateTransactionRequest;
import com.financeledger.dto.TransactionResponse;
import com.financeledger.entity.IdempotencyRecord;
import com.financeledger.entity.IdempotencyStatus;
import com.financeledger.entity.Transaction;
import com.financeledger.entity.User;
import com.financeledger.security.CustomUserDetailsService;
import com.financeledger.service.IdempotencyService;
import com.financeledger.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for transaction operations with idempotency support.
 *
 * <p>The {@code Idempotency-Key} header is required for {@code POST} requests.
 * Behavior on duplicate keys:
 * <ul>
 *   <li>COMPLETED → replay cached response (200)</li>
 *   <li>PROCESSING → 409 Conflict (concurrent duplicate)</li>
 *   <li>FAILED → 422 Unprocessable Entity (retry with new key)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public TransactionController(TransactionService transactionService,
                                 IdempotencyService idempotencyService,
                                 CustomUserDetailsService userDetailsService) {
        this.transactionService = transactionService;
        this.idempotencyService = idempotencyService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Create a new transaction with idempotency support.
     *
     * <p>Requires {@code Idempotency-Key} header. The full flow:
     * <ol>
     *   <li>Claim the idempotency key (atomic INSERT ON CONFLICT in REQUIRES_NEW tx).</li>
     *   <li>If key is new → create transaction, cache response, return 201.</li>
     *   <li>If key exists and COMPLETED → replay cached response.</li>
     *   <li>If key exists and PROCESSING → return 409 (concurrent duplicate).</li>
     *   <li>If key exists and FAILED → return 422 (retry with new key).</li>
     * </ol>
     */
    @PostMapping
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        User user = userDetailsService.findUserByEmail(authentication.getName());

        // 1. Attempt to claim the idempotency key
        Optional<IdempotencyRecord> existingRecord = idempotencyService.claimKey(idempotencyKey, user.getId());

        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();

            return switch (record.getStatus()) {
                case COMPLETED -> {
                    log.info("Replaying idempotent response for key={}", idempotencyKey);
                    yield ResponseEntity.status(record.getResponseStatus())
                            .body(record.getResponseBody());
                }
                case PROCESSING -> {
                    log.warn("Concurrent duplicate detected for key={}", idempotencyKey);
                    yield ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("message", "Request with this Idempotency-Key is already being processed"));
                }
                case FAILED -> {
                    log.warn("Previous attempt failed for key={}", idempotencyKey);
                    yield ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .body(Map.of("message", "Previous attempt with this key failed. Please retry with a new Idempotency-Key"));
                }
            };
        }

        // 2. Key claimed — proceed with transaction creation
        try {
            Transaction transaction = transactionService.createTransaction(request, user.getId());
            TransactionResponse response = transactionService.toResponse(transaction);
            String responseJson;
            try {
                responseJson = objectMapper.writeValueAsString(response);
            } catch (Exception jsonEx) {
                responseJson = "{}";
            }

            // 3. Mark key as completed with cached response
            idempotencyService.markCompleted(idempotencyKey, transaction.getId(), responseJson, 201);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // 4. Mark key as failed
            idempotencyService.markFailed(idempotencyKey);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listTransactions(Authentication authentication) {
        User user = userDetailsService.findUserByEmail(authentication.getName());
        List<TransactionResponse> transactions = transactionService.getTransactionsByUser(user.getId()).stream()
                .map(transactionService::toResponse)
                .toList();
        return ResponseEntity.ok(transactions);
    }
}
