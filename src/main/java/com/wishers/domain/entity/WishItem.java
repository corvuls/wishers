package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wish_items")
public class WishItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private WishList wishList;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String link;

    @Column(length = 64)
    private String price;

    @Column(length = 2000)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }

    public WishList getWishList() { return wishList; }
    public void setWishList(WishList wishList) { this.wishList = wishList; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
