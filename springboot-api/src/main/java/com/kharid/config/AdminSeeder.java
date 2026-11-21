package com.kharid.config;

import com.kharid.entity.AppUser;
import com.kharid.repository.AppUserRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the admin account on first start. */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminSeeder(AppUserRepository users, PasswordEncoder encoder,
                       @Value("${app.admin.email}") String email, @Value("${app.admin.password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(email)) {
            AppUser admin = new AppUser();
            admin.setName("Administrator");
            admin.setEmail(email);
            admin.setPasswordHash(encoder.encode(password));
            admin.setRole("ADMIN");
            admin.setCreatedAt(Instant.now());
            users.save(admin);
        }
    }
}
