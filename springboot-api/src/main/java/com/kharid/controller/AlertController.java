package com.kharid.controller;

import com.kharid.dto.AlertRequest;
import com.kharid.entity.AppUser;
import com.kharid.entity.PriceAlert;
import com.kharid.repository.PriceAlertRepository;
import com.kharid.service.ListingService;
import com.kharid.service.MlService;
import com.kharid.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Price-drop alerts. Listing a user's alerts re-checks the current lowest prices. */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final PriceAlertRepository repository;
    private final UserService users;
    private final MlService ml;
    private final ListingService listingService;

    public AlertController(PriceAlertRepository repository, UserService users, MlService ml, ListingService listingService) {
        this.repository = repository;
        this.users = users;
        this.ml = ml;
        this.listingService = listingService;
    }

    @GetMapping
    public List<PriceAlert> list() {
        AppUser u = users.require();
        List<PriceAlert> alerts = repository.findByUserIdOrderByIdDesc(u.getId());
        if (!alerts.isEmpty()) {
            listingService.ensureSeeded();
            Map<String, Integer> lows = new HashMap<>();
            for (Map<String, Object> c : ml.catalogue(listingService.payload())) {
                lows.put((String) c.get("id"), ((Number) c.get("low")).intValue());
            }
            for (PriceAlert a : alerts) {
                Integer current = lows.get(a.getProductId());
                if (current != null) {
                    a.setCurrentPrice(current);
                    if (!a.isTriggered() && current <= a.getTargetPrice()) {
                        a.setTriggered(true);
                        a.setTriggeredAt(Instant.now());
                    }
                }
            }
            repository.saveAll(alerts);
        }
        return alerts;
    }

    @PostMapping
    public PriceAlert create(@Valid @RequestBody AlertRequest request) {
        AppUser u = users.require();
        PriceAlert a = new PriceAlert();
        a.setUserId(u.getId());
        a.setProductId(request.productId());
        a.setProductName(request.productName());
        a.setTargetPrice(request.targetPrice());
        a.setCurrentPrice(request.currentPrice());
        a.setCreatedAt(Instant.now());
        return repository.save(a);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        AppUser u = users.require();
        PriceAlert a = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found."));
        if (!a.getUserId().equals(u.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This alert belongs to another user.");
        }
        repository.delete(a);
    }
}
