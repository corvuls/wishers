package com.wishers.controller;

import com.wishers.repo.UserRepository;
import com.wishers.domain.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow();
        model.addAttribute("user", user);
        return "profile/index";
    }
}
