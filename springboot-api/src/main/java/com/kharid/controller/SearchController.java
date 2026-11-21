package com.kharid.controller;

import com.kharid.dto.SearchRequest;
import com.kharid.entity.AppUser;
import com.kharid.entity.SearchLog;
import com.kharid.repository.SearchLogRepository;
import com.kharid.service.ListingService;
import com.kharid.service.MlService;
import com.kharid.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final MlService mlService;
    private final ListingService listingService;
    private final SearchLogRepository repository;
    private final UserService users;

    public SearchController(MlService mlService, ListingService listingService, SearchLogRepository repository,
                            UserService users) {
        this.mlService = mlService;
        this.listingService = listingService;
        this.repository = repository;
        this.users = users;
    }

    @PostMapping("/search")
    public Map<String, Object> search(@Valid @RequestBody SearchRequest request) {
        String text = request.query().trim();
        AppUser user = users.current();
        listingService.ensureSeeded();
        Map<String, Object> result = mlService.analyze(text, listingService.payload(), users.mlProfile(user));

        SearchLog log = new SearchLog();
        log.setUserId(user == null ? null : user.getId());
        log.setSearchText(text);
        log.setCreatedAt(Instant.now());
        Object requirements = result.get("requirements");
        if (requirements instanceof Map<?, ?> map) {
            Object category = map.get("category");
            if (category != null) {
                log.setCategory(category.toString());
            }
            if (map.get("budget") instanceof Number budget) {
                log.setBudget(budget.intValue());
            }
        }
        repository.save(log);
        return result;
    }

    @GetMapping("/history")
    public List<Map<String, Object>> history() {
        AppUser user = users.current();
        if (user == null) {
            return List.of();
        }
        return repository.findTop8ByUserIdOrderByIdDesc(user.getId()).stream()
                .map(l -> Map.<String, Object>of("id", l.getId(), "query", l.getSearchText()))
                .toList();
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("api", "ok", "mlService", mlService.isUp() ? "ok" : "down");
    }
}
