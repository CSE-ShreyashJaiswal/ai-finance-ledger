package com.financeledger.dto;

/**
 * Response DTO containing a JWT token after successful registration or login.
 */
public record AuthResponse(
        String token,
        String email,
        Long userId
) {
}
