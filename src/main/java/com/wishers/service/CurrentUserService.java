package com.wishers.service;

import com.wishers.domain.entity.User;
import com.wishers.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
  private final UserRepository userRepository;

  public CurrentUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User requireUser() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    String email = a == null ? null : a.getName();
    if (email == null) throw new IllegalStateException("No auth");
    return userRepository.findByEmailIgnoreCase(email).orElseThrow();
  }
}
