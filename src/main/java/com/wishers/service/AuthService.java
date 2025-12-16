package com.wishers.service;

import com.wishers.domain.entity.User;
import com.wishers.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  public AuthService(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Transactional
  public User register(String email, String password, String nickname, String phone) {
    if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
    if (password == null || password.isBlank()) throw new IllegalArgumentException("Password required");
    if (nickname == null || nickname.isBlank()) throw new IllegalArgumentException("Nickname required");
    if (phone == null || phone.isBlank()) throw new IllegalArgumentException("Phone required");

    if (users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email already used");
    if (users.existsByNicknameIgnoreCase(nickname)) throw new IllegalArgumentException("Nickname already used");

    User u = new User();
    u.setEmail(email.trim().toLowerCase());
    u.setNickname(nickname.trim());
    u.setPhone(phone.trim());
    u.setPasswordHash(encoder.encode(password));
    return users.save(u);
  }
}
