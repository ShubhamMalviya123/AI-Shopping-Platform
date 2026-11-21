package com.kharid.controller;

import com.kharid.entity.PriceAlert;
import com.kharid.entity.SearchLog;
import com.kharid.repository.AppUserRepository;
import com.kharid.repository.ListingRepository;
import com.kharid.repository.PriceAlertRepository;
import com.kharid.repository.SavedProductRepository;
import com.kharid.repository.SearchLogRepository;
import com.kharid.service.ListingService;
import com.kharid.service.MlService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Administration and monitoring (ADMIN role only). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AppUserRepository userRepo;
    private final SearchLogRepository logRepo;
    private final ListingRepository listingRepo;
    private final SavedProductRepository savedRepo;
    private final PriceAlertRepository alertRepo;
    private final MlService ml;
    private final ListingService listingService;

    public AdminController(AppUserRepository userRepo, SearchLogRepository logRepo, ListingRepository listingRepo,
                           SavedProductRepository savedRepo, PriceAlertRepository alertRepo, MlService ml,
                           ListingService listingService) {
        this.userRepo = userRepo;
        this.logRepo = logRepo;
        this.listingRepo = listingRepo;
        this.savedRepo = savedRepo;
        this.alertRepo = alertRepo;
        this.ml = ml;
        this.listingService = listingService;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("users", userRepo.count());
        m.put("searches", logRepo.count());
        m.put("listings", listingRepo.count());
        m.put("savedProducts", savedRepo.count());
        m.put("alerts", alertRepo.count());
        m.put("triggeredAlerts", alertRepo.findAll().stream().filter(PriceAlert::isTriggered).count());
        m.put("mlService", ml.isUp() ? "ok" : "down");
        m.put("topCategories", logRepo.findAll().stream().filter(l -> l.getCategory() != null)
                .collect(Collectors.groupingBy(SearchLog::getCategory, Collectors.counting())));
        return m;
    }

    @PostMapping("/simulate-drop")
    public Map<String, Object> simulateDrop() {
        listingService.ensureSeeded();
        return Map.of("changes", listingService.simulateDrop());
    }
}
