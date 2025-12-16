package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wish_funds")
public class WishFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private WishItem wishItem;

    @Column(nullable = false)
    private long targetAmountRub; // цель в рублях (целое)

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }

    public WishItem getWishItem() { return wishItem; }
    public void setWishItem(WishItem wishItem) { this.wishItem = wishItem; }

    public long getTargetAmountRub() { return targetAmountRub; }
    public void setTargetAmountRub(long targetAmountRub) { this.targetAmountRub = targetAmountRub; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
