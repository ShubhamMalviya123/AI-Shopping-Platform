package com.kharid.entity;

import jakarta.persistence.*;

/** One product listing on one marketplace (raw data before AI matching). */
@Entity
@Table(name = "listing")
public class Listing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "external_id", unique = true) private String externalId;
    private String marketplace;
    @Column(length = 300) private String title;
    private String brand;
    private String category;
    private Integer price;
    private Double rating;
    private Integer ratingCount;
    private Integer ram;
    @Column(length = 500) private String specJson;
    @Column(length = 500) private String url;

    public Long getId() { return id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String v) { this.externalId = v; }
    public String getMarketplace() { return marketplace; }
    public void setMarketplace(String v) { this.marketplace = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getBrand() { return brand; }
    public void setBrand(String v) { this.brand = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer v) { this.price = v; }
    public Double getRating() { return rating; }
    public void setRating(Double v) { this.rating = v; }
    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer v) { this.ratingCount = v; }
    public Integer getRam() { return ram; }
    public void setRam(Integer v) { this.ram = v; }
    public String getSpecJson() { return specJson; }
    public void setSpecJson(String v) { this.specJson = v; }
    public String getUrl() { return url; }
    public void setUrl(String v) { this.url = v; }
}
