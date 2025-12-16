package com.wishers.service;

import com.wishers.repo.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

  private final UserRepository users;

  public AuthUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var u = users.findByEmailIgnoreCase(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return User.withUsername(u.getEmail())
        .password(u.getPasswordHash())
        .roles("USER")
        .build();
  }
}
