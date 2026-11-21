package com.kharid.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preference")
public class UserPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true) private Long userId;
    @Column(length = 300) private String preferredBrands;
    private Integer budget;
    @Column(length = 300) private String priorities;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getPreferredBrands() { return preferredBrands; }
    public void setPreferredBrands(String v) { this.preferredBrands = v; }
    public Integer getBudget() { return budget; }
    public void setBudget(Integer v) { this.budget = v; }
    public String getPriorities() { return priorities; }
    public void setPriorities(String v) { this.priorities = v; }
}
