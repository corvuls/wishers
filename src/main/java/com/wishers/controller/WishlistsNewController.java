package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishList;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishListRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class WishlistsNewController {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;

    public WishlistsNewController(WishListRepository wishListRepository, UserRepository userRepository) {
        this.wishListRepository = wishListRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/wishlists/new")
    public String newWishlistPage() {
        return "wishlists/new";
    }

    @PostMapping("/wishlists")
    public String createWishlist(@RequestParam String title,
                                 @RequestParam(required = false) String description,
                                 Principal principal,
                                 RedirectAttributes ra) {

        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) {
            ra.addFlashAttribute("formError", "Название не должно быть пустым");
            return "redirect:/wishlists/new";
        }

        User me = userRepository.findByEmail(principal.getName()).orElseThrow();

        WishList wl = new WishList();
        wl.setOwner(me);
        wl.setTitle(t);
        wl.setDescription(description == null ? null : description.trim());

        wl = wishListRepository.save(wl);

        return "redirect:/wishlists/" + wl.getId();
    }
}
