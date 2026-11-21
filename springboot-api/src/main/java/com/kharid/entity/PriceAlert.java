package com.kharid.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "price_alert")
public class PriceAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long userId;
    private String productId;
    @Column(length = 300) private String productName;
    private Integer targetPrice;
    private Integer currentPrice;
    private boolean triggered;
    private Instant createdAt;
    private Instant triggeredAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long v) { this.userId = v; }
    public String getProductId() { return productId; }
    public void setProductId(String v) { this.productId = v; }
    public String getProductName() { return productName; }
    public void setProductName(String v) { this.productName = v; }
    public Integer getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Integer v) { this.targetPrice = v; }
    public Integer getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Integer v) { this.currentPrice = v; }
    public boolean isTriggered() { return triggered; }
    public void setTriggered(boolean v) { this.triggered = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant v) { this.triggeredAt = v; }
}
