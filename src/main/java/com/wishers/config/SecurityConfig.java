package com.wishers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth
          .requestMatchers(
              "/login", "/register",
              "/css/**", "/js/**", "/images/**", "/favicon.ico"
          ).permitAll()
          .requestMatchers("/h2-console/**").permitAll()
          .anyRequest().authenticated()
      )
      .formLogin(form -> form
          .loginPage("/login")
          .loginProcessingUrl("/login")
          .defaultSuccessUrl("/feed", true)
          .failureUrl("/login?error")
          .permitAll()
      )
      .logout(logout -> logout
          .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
          .logoutSuccessUrl("/login?logout")
          .permitAll()
      );

    // H2 console support
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
    http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    return http.build();
  }
}
