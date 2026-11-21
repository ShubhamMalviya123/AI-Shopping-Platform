package com.kharid.repository;

import com.kharid.entity.SearchLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
    List<SearchLog> findTop8ByUserIdOrderByIdDesc(Long userId);

    List<SearchLog> findTop10ByUserIdOrderByIdDesc(Long userId);
}
