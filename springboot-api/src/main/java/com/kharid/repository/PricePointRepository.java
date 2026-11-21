package com.kharid.repository;

import com.kharid.entity.PricePoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricePointRepository extends JpaRepository<PricePoint, Long> {
}
