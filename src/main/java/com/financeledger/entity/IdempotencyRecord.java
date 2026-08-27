package com.financeledger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Tracks the lifecycle of an idempotency key to prevent duplicate transactions.
 *
 * <p><b>Design (from v3 plan, Section 5.2):</b>
 * <ol>
 *   <li>Key is claimed via {@code INSERT ... ON CONFLICT} in a separate
 *       {@code REQUIRES_NEW} transaction — this is atomic and race-free.</li>
 *   <li>If the key is new → status = PROCESSING, proceed with transaction creation.</li>
 *   <li>If the key already exists:
 *       <ul>
 *         <li>COMPLETED → replay the cached response (200 OK)</li>
 *         <li>PROCESSING → return 409 Conflict (concurrent duplicate)</li>
 *         <li>FAILED → return 422 (client should retry with a new key)</li>
 *       </ul>
 *   </li>
 *   <li>After transaction creation, status is updated to COMPLETED with cached response.</li>
 * </ol>
 */
@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, User user) {
        this.idempotencyKey = idempotencyKey;
        this.user = user;
        this.status = IdempotencyStatus.PROCESSING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }

    public String getIdempotencyKey() { return idempotencyKey; }

    public User getUser() { return user; }

    public IdempotencyStatus getStatus() { return status; }
    public void setStatus(IdempotencyStatus status) { this.status = status; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
