package com.kharid.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "price_point")
public class PricePoint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long listingId;
    private Integer monthIndex;
    private Integer price;

    public Long getId() { return id; }
    public Long getListingId() { return listingId; }
    public void setListingId(Long v) { this.listingId = v; }
    public Integer getMonthIndex() { return monthIndex; }
    public void setMonthIndex(Integer v) { this.monthIndex = v; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer v) { this.price = v; }
}
