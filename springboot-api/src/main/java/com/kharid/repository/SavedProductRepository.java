package com.kharid.repository;

import com.kharid.entity.SavedProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedProductRepository extends JpaRepository<SavedProduct, Long> {
    List<SavedProduct> findByUserIdOrderByIdDesc(Long userId);

    Optional<SavedProduct> findByUserIdAndProductId(Long userId, String productId);
}
