package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishListRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class WishlistsController {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;

    public WishlistsController(WishListRepository wishListRepository, UserRepository userRepository) {
        this.wishListRepository = wishListRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/wishlists")
    public String wishlists(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow();
        model.addAttribute("wishlists", wishListRepository.findAllByOwner(user));
        return "wishlists/index";
    }
}
