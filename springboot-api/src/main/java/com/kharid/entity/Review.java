package com.kharid.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "review")
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long listingId;
    @Column(name = "review_text", length = 500) private String text;

    public Long getId() { return id; }
    public Long getListingId() { return listingId; }
    public void setListingId(Long v) { this.listingId = v; }
    public String getText() { return text; }
    public void setText(String v) { this.text = v; }
}
