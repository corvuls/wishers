package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name="wishlists")
public class WishList {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  private User owner;

  @Column(nullable=false, length=120)
  private String title;

  @Column(length=1000)
  private String description;

  private LocalDate eventDate;

  public Long getId() { return id; }

  public User getOwner() { return owner; }
  public void setOwner(User owner) { this.owner = owner; }

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public LocalDate getEventDate() { return eventDate; }
  public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
}
