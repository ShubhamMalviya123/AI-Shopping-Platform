package com.kharid.controller;

import com.kharid.dto.ProfileRequest;
import com.kharid.entity.AppUser;
import com.kharid.entity.UserPreference;
import com.kharid.repository.AppUserRepository;
import com.kharid.repository.UserPreferenceRepository;
import com.kharid.service.UserService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService users;
    private final AppUserRepository userRepo;
    private final UserPreferenceRepository prefRepo;

    public ProfileController(UserService users, AppUserRepository userRepo, UserPreferenceRepository prefRepo) {
        this.users = users;
        this.userRepo = userRepo;
        this.prefRepo = prefRepo;
    }

    @GetMapping
    public Map<String, Object> get() {
        AppUser u = users.require();
        return view(u, users.preferenceOf(u));
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody ProfileRequest request) {
        AppUser u = users.require();
        if (request.name() != null && !request.name().isBlank()) {
            u.setName(request.name().trim());
            userRepo.save(u);
        }
        UserPreference p = users.preferenceOf(u);
        p.setPreferredBrands(request.preferredBrands());
        p.setBudget(request.budget());
        p.setPriorities(request.priorities());
        return view(u, prefRepo.save(p));
    }

    private Map<String, Object> view(AppUser u, UserPreference p) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("preferredBrands", p.getPreferredBrands() == null ? "" : p.getPreferredBrands());
        m.put("budget", p.getBudget());
        m.put("priorities", p.getPriorities() == null ? "" : p.getPriorities());
        return m;
    }
}
