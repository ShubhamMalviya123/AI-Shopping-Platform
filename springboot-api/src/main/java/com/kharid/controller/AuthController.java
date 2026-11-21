package com.kharid.controller;

import com.kharid.dto.LoginRequest;
import com.kharid.dto.RegisterRequest;
import com.kharid.entity.AppUser;
import com.kharid.repository.AppUserRepository;
import com.kharid.security.JwtService;
import com.kharid.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final UserService userService;

    public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwt, UserService userService) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.userService = userService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered.");
        }
        AppUser u = new AppUser();
        u.setName(request.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(request.password()));
        u.setRole("USER");
        u.setCreatedAt(Instant.now());
        return response(users.save(u));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        AppUser u = users.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password."));
        if (!encoder.matches(request.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password.");
        }
        return response(u);
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        return userMap(userService.require());
    }

    private Map<String, Object> response(AppUser u) {
        return Map.of("token", jwt.create(u.getEmail(), u.getRole()), "user", userMap(u));
    }

    private Map<String, Object> userMap(AppUser u) {
        return Map.of("name", u.getName(), "email", u.getEmail(), "role", u.getRole());
    }
}
