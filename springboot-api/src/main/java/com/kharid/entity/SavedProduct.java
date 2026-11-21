package com.kharid.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "saved_product")
public class SavedProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long userId;
    private String productId;
    @Column(length = 300) private String name;
    private Integer price;
    private String marketplace;
    @Column(length = 500) private String url;
    private Instant savedAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getProductId() { return productId; }
    public void setProductId(String v) { this.productId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer v) { this.price = v; }
    public String getMarketplace() { return marketplace; }
    public void setMarketplace(String v) { this.marketplace = v; }
    public String getUrl() { return url; }
    public void setUrl(String v) { this.url = v; }
    public Instant getSavedAt() { return savedAt; }
    public void setSavedAt(Instant v) { this.savedAt = v; }
}
