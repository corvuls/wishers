package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishItem;
import com.wishers.domain.entity.WishList;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishItemRepository;
import com.wishers.repo.WishListRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class WishlistsController {

  private final WishListRepository wishListRepository;
  private final WishItemRepository wishItemRepository;
  private final UserRepository userRepository;

  public WishlistsController(WishListRepository wishListRepository,
                             WishItemRepository wishItemRepository,
                             UserRepository userRepository) {
    this.wishListRepository = wishListRepository;
    this.wishItemRepository = wishItemRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/wishlists")
  public String wishlists(Principal principal, Model model) {
    User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();
    model.addAttribute("wishlists", wishListRepository.findAllByOwner(user));
    return "wishlists/index";
  }

  @PostMapping("/wishlists/{id}/delete")
  public String deleteWishlist(@PathVariable Long id, Principal principal) {
    User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

    WishList wl = wishListRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!wl.getOwner().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    // сначала удаляем подарки, иначе FK не даст удалить вишлист
    wishItemRepository.deleteByWishList(wl);
    wishListRepository.delete(wl);

    return "redirect:/wishlists";
  }

  @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/delete")
  public String deleteWish(@PathVariable Long wishlistId,
                           @PathVariable Long wishId,
                           Principal principal) {
    User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

    WishList wl = wishListRepository.findById(wishlistId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!wl.getOwner().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    WishItem item = wishItemRepository.findById(wishId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!item.getWishList().getId().equals(wl.getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    wishItemRepository.delete(item);
    return "redirect:/wishlists/" + wishlistId;
  }
}
