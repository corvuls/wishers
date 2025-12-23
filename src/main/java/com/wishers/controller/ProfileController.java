package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.UUID;

@Controller
public class ProfileController {

  private final UserRepository userRepository;

  @Value("${wishers.upload-dir:./data/uploads}")
  private String uploadDir;

  public ProfileController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/profile")
  public String profile(Principal principal, Model model) {
    User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();
    model.addAttribute("user", user);
    return "profile/index";
  }

  @PostMapping("/profile/avatar")
  public String uploadAvatar(Principal principal,
                             @RequestParam("avatar") MultipartFile avatar) {
    User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

    if (avatar == null || avatar.isEmpty()) {
      return "redirect:/profile";
    }

    user.setAvatarPath("/uploads/" + saveImage(avatar));
    userRepository.save(user);

    return "redirect:/profile";
  }

  private String saveImage(MultipartFile file) {
    try {
      Files.createDirectories(Path.of(uploadDir));

      String original = file.getOriginalFilename();
      String ext = "";
      if (original != null) {
        int i = original.lastIndexOf('.');
        if (i >= 0 && i < original.length() - 1) {
          String candidate = original.substring(i).toLowerCase();
          if (candidate.equals(".png") || candidate.equals(".jpg") || candidate.equals(".jpeg") || candidate.equals(".webp")) {
            ext = candidate;
          }
        }
      }

      String name = UUID.randomUUID() + ext;
      Files.copy(file.getInputStream(), Path.of(uploadDir).resolve(name).normalize());
      return name;
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось сохранить аватар");
    }
  }
}
