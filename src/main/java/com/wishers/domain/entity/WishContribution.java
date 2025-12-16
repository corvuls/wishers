package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wish_contributions")
public class WishContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private WishFund fund;

    @ManyToOne(fetch = FetchType.LAZY)
    private User contributor; // может быть null, если анонимно (пока оставим)

    @Column(nullable = false)
    private long amountRub; // сумма в рублях (целое)

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }

    public WishFund getFund() { return fund; }
    public void setFund(WishFund fund) { this.fund = fund; }

    public User getContributor() { return contributor; }
    public void setContributor(User contributor) { this.contributor = contributor; }

    public long getAmountRub() { return amountRub; }
    public void setAmountRub(long amountRub) { this.amountRub = amountRub; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
