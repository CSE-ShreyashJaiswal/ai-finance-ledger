package com.financeledger.controller;

import com.financeledger.dto.AuthResponse;
import com.financeledger.dto.LoginRequest;
import com.financeledger.dto.RegisterRequest;
import com.financeledger.entity.User;
import com.financeledger.repository.UserRepository;
import com.financeledger.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Authentication controller — register and login endpoints.
 *
 * <p>Both endpoints are publicly accessible ({@code /api/auth/**} is excluded
 * from JWT authentication in {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Register a new user.
     *
     * <pre>POST /api/auth/register</pre>
     *
     * @return JWT token on success, 409 if email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already registered"));
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getEmail(), user.getId()));
    }

    /**
     * Login with existing credentials.
     *
     * <pre>POST /api/auth/login</pre>
     *
     * @return JWT token on success, 401 if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getId()));
    }
}
