package com.kharid.controller;

import com.kharid.dto.SavedRequest;
import com.kharid.entity.AppUser;
import com.kharid.entity.SavedProduct;
import com.kharid.repository.SavedProductRepository;
import com.kharid.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Wishlist of the logged-in user. */
@RestController
@RequestMapping("/api/saved")
public class SavedController {

    private final SavedProductRepository repository;
    private final UserService users;

    public SavedController(SavedProductRepository repository, UserService users) {
        this.repository = repository;
        this.users = users;
    }

    @GetMapping
    public List<SavedProduct> list() {
        return repository.findByUserIdOrderByIdDesc(users.require().getId());
    }

    @PostMapping
    public SavedProduct save(@Valid @RequestBody SavedRequest request) {
        AppUser u = users.require();
        return repository.findByUserIdAndProductId(u.getId(), request.productId()).orElseGet(() -> {
            SavedProduct p = new SavedProduct();
            p.setUserId(u.getId());
            p.setProductId(request.productId());
            p.setName(request.name());
            p.setPrice(request.price());
            p.setMarketplace(request.marketplace());
            p.setUrl(request.url());
            p.setSavedAt(Instant.now());
            return repository.save(p);
        });
    }

    @DeleteMapping("/{productId}")
    public void remove(@PathVariable String productId) {
        AppUser u = users.require();
        repository.findByUserIdAndProductId(u.getId(), productId).ifPresent(repository::delete);
    }
}
