package com.kharid.service;

import com.kharid.entity.AppUser;
import com.kharid.entity.UserPreference;
import com.kharid.repository.AppUserRepository;
import com.kharid.repository.UserPreferenceRepository;
import com.kharid.security.CurrentUser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final AppUserRepository users;
    private final UserPreferenceRepository prefs;

    public UserService(AppUserRepository users, UserPreferenceRepository prefs) {
        this.users = users;
        this.prefs = prefs;
    }

    /** Logged-in user or null. */
    public AppUser current() {
        String email = CurrentUser.email();
        return email == null ? null : users.findByEmail(email).orElse(null);
    }

    public AppUser require() {
        AppUser u = current();
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first.");
        }
        return u;
    }

    public UserPreference preferenceOf(AppUser u) {
        return prefs.findByUserId(u.getId()).orElseGet(() -> {
            UserPreference p = new UserPreference();
            p.setUserId(u.getId());
            return p;
        });
    }

    public List<String> brandsOf(AppUser u) {
        if (u == null) {
            return List.of();
        }
        String csv = preferenceOf(u).getPreferredBrands();
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /** Preferences in the format the Python AI service expects. */
    public Map<String, Object> mlProfile(AppUser u) {
        Map<String, Object> m = new HashMap<>();
        m.put("preferred_brands", brandsOf(u));
        m.put("default_budget", u == null ? null : preferenceOf(u).getBudget());
        return m;
    }
}
