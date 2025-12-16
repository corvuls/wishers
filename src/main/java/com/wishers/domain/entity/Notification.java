package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="notifications")
public class Notification {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  private User user;

  @Column(nullable=false, length=64)
  private String type;

  @Column(nullable=false, length=512)
  private String message;

  @Column(nullable=false)
  private Instant createdAt;

  @Column(nullable=false)
  private boolean readFlag = false;

  @Column(length=80)
  private String dedupeKey;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  public Instant getCreatedAt() { return createdAt; }
  public boolean isReadFlag() { return readFlag; }
  public void setReadFlag(boolean readFlag) { this.readFlag = readFlag; }
  public String getDedupeKey() { return dedupeKey; }
  public void setDedupeKey(String dedupeKey) { this.dedupeKey = dedupeKey; }
}
