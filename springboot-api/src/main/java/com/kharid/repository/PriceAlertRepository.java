package com.kharid.repository;

import com.kharid.entity.PriceAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByUserIdOrderByIdDesc(Long userId);
}
