package com.financeledger.controller;

import com.financeledger.dto.CreateTransactionRequest;
import com.financeledger.dto.TransactionResponse;
import com.financeledger.entity.Transaction;
import com.financeledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for transaction operations.
 *
 * <p>Each transaction creates balanced double-entry ledger records.
 * See {@link TransactionService#createTransaction} for the core logic.
 *
 * <p>Note: {@code Idempotency-Key} header support is added in Week 3.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Create a new transaction with balanced double-entry ledger entries.
     *
     * <pre>POST /api/transactions</pre>
     *
     * <p>Request body example:
     * <pre>
     * {
     *   "userId": 1,
     *   "description": "Groceries at Whole Foods",
     *   "amount": 45.50,
     *   "debitAccountId": 2,
     *   "creditAccountId": 1,
     *   "category": "FOOD"
     * }
     * </pre>
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.toResponse(transaction));
    }

    /**
     * List all transactions for a user, most recent first.
     *
     * <pre>GET /api/transactions?userId=1</pre>
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listTransactions(@RequestParam Long userId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByUser(userId).stream()
                .map(transactionService::toResponse)
                .toList();
        return ResponseEntity.ok(transactions);
    }
}
