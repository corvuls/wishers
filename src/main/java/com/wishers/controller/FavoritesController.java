package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishList;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishListRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/favorites")
public class FavoritesController {

  private final UserRepository users;
  private final WishListRepository lists;

  public FavoritesController(UserRepository users, WishListRepository lists) {
    this.users = users;
    this.lists = lists;
  }

  @GetMapping
  public String page(Authentication auth, Model model) {
    User user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
    model.addAttribute("lists", lists.findAllByOwnerOrderByIdDesc(user));
    return "favorites/index";
  }

  @PostMapping
  public String create(Authentication auth,
                       @RequestParam("title") String title,
                       @RequestParam(value="description", required=false) String description) {
    if (title == null || title.isBlank()) return "redirect:/favorites";
    User user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
    WishList wl = new WishList();
    wl.setOwner(user);
    wl.setTitle(title.trim());
    wl.setDescription(description == null ? null : description.trim());
    lists.save(wl);
    return "redirect:/favorites";
  }
}
