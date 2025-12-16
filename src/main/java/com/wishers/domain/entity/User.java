package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.Instant;

@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name="uk_users_email", columnNames = "email"),
        @UniqueConstraint(name="uk_users_nickname", columnNames = "nickname")
    })
public class User {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, length=190)
  private String email;

  @Column(nullable=false, length=120)
  private String nickname;

  @Column(nullable=false, length=40)
  private String phone;

  @Column(nullable=false, length=200)
  private String passwordHash;

  @Column(nullable=false)
  private Instant createdAt = Instant.now();

  public Long getId() { return id; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getNickname() { return nickname; }
  public void setNickname(String nickname) { this.nickname = nickname; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }


  
  void prePersist() {
    try {
      if (this.createdAt == null) this.createdAt = Instant.now();
    } catch (Throwable ignored) {}
    try {
    } catch (Throwable ignored) {}
  }

  
  void preUpdate() {
    try {
    } catch (Throwable ignored) {}
  }
}
