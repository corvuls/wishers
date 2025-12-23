package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
  name = "friendships",
  uniqueConstraints = @UniqueConstraint(name="uk_friend_pair", columnNames = {"user_a_id","user_b_id"})
)
public class Friendship {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_a_id", nullable = false)
  private User userA;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_b_id", nullable = false)
  private User userB;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public Long getId() { return id; }

  public User getUserA() { return userA; }
  public void setUserA(User userA) { this.userA = userA; }

  public User getUserB() { return userB; }
  public void setUserB(User userB) { this.userB = userB; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
