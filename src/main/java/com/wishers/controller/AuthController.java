package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.time.Instant;

@Controller
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("/login")
  public String login() {
    return "auth/login";
  }

  @GetMapping("/register")
  public String register() {
    return "auth/register";
  }

  @PostMapping("/register")
  public String doRegister(
      @RequestParam String email,
      @RequestParam String password,
      @RequestParam String nickname,
      @RequestParam String phone,
      Model model
  ) {
    String e = email == null ? "" : email.trim().toLowerCase();
    String n = nickname == null ? "" : nickname.trim();
    String p = phone == null ? "" : phone.trim();
    String raw = password == null ? "" : password;

    if (e.isBlank() || n.isBlank() || p.isBlank()) {
      model.addAttribute("formError", "Заполни все поля.");
      return "auth/register";
    }

    if (raw.length() < 5) {
      model.addAttribute("formError", "Пароль должен быть минимум 5 символов.");
      return "auth/register";
    }

    if (userRepository.existsByEmailIgnoreCase(e)) {
      model.addAttribute("formError", "Пользователь с таким email уже существует.");
      return "auth/register";
    }

    if (userRepository.existsByNicknameIgnoreCase(n)) {
      model.addAttribute("formError", "Этот никнейм уже занят.");
      return "auth/register";
    }

    try {
      User u = new User();
      u.setEmail(e);
      u.setNickname(n);
      u.setPhone(p);
      u.setPasswordHash(passwordEncoder.encode(raw));
      setInstantIfSetterExists(u, "setCreatedAt");
      userRepository.save(u);
      return "redirect:/login?registered";
    } catch (Exception ex) {
      model.addAttribute("formError", rootMessage(ex));
      return "auth/register";
    }
  }

  private static void setInstantIfSetterExists(Object target, String setterName) {
    try {
      Method m = target.getClass().getMethod(setterName, Instant.class);
      m.invoke(target, Instant.now());
    } catch (Exception ignored) {
    }
  }

  private static String rootMessage(Throwable t) {
    Throwable r = t;
    while (r.getCause() != null) r = r.getCause();
    String msg = r.getMessage();
    if (msg == null || msg.isBlank()) msg = r.getClass().getSimpleName();
    if (msg.length() > 250) msg = msg.substring(0, 250);
    return "Ошибка регистрации: " + msg;
  }
}
