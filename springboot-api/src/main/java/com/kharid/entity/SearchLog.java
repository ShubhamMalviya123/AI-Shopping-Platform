package com.kharid.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "search_log")
public class SearchLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long userId;
    @Column(name = "search_text", length = 400) private String searchText;
    private String category;
    private Integer budget;
    private Instant createdAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getSearchText() { return searchText; }
    public void setSearchText(String v) { this.searchText = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public Integer getBudget() { return budget; }
    public void setBudget(Integer v) { this.budget = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
}
