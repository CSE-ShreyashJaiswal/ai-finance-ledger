package com.financeledger.repository;

import com.financeledger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link User} entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address (used for login/auth in Week 3).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email already exists (used for registration).
     */
    boolean existsByEmail(String email);
}
