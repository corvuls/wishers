package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishItem;
import com.wishers.domain.entity.WishList;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishItemRepository;
import com.wishers.repo.WishListRepository;
import com.wishers.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;

@Controller
public class ReservationController {

  private final UserRepository userRepository;
  private final WishListRepository wishListRepository;
  private final WishItemRepository wishItemRepository;
  private final NotificationService notificationService;

  public ReservationController(UserRepository userRepository,
                               WishListRepository wishListRepository,
                               WishItemRepository wishItemRepository,
                               NotificationService notificationService) {
    this.userRepository = userRepository;
    this.wishListRepository = wishListRepository;
    this.wishItemRepository = wishItemRepository;
    this.notificationService = notificationService;
  }

  // GET -> редирект обратно (чтобы не было 405 если открыть руками)
  @GetMapping("/wishlists/{wishlistId}/wishes/{wishId}/reserve")
  public String reserveGet(@PathVariable Long wishlistId, @PathVariable Long wishId) {
    return "redirect:/wishlists/" + wishlistId;
  }

  @GetMapping("/wishlists/{wishlistId}/wishes/{wishId}/unreserve")
  public String unreserveGet(@PathVariable Long wishlistId, @PathVariable Long wishId) {
    return "redirect:/wishlists/" + wishlistId;
  }

  // Реальная бронь — POST
  @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/reserve")
  public String reserve(@PathVariable Long wishlistId,
                        @PathVariable Long wishId,
                        Principal principal) {

    User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

    WishList wl = wishListRepository.findById(wishlistId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // Нельзя бронировать свои же подарки
    if (wl.getOwner().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    WishItem item = wishItemRepository.findById(wishId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!item.getWishList().getId().equals(wl.getId())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    // Если уже забронировано другим — конфликт
    if (item.getReservedBy() != null && !item.getReservedBy().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT);
    }

    item.setReservedBy(me);
    item.setReservedAt(Instant.now());
    wishItemRepository.save(item);

    // Уведомление владельцу: БЕЗ имени бронирующего (анонимно)
    String msg = "Кто-то забронировал подарок: " + item.getTitle();
    notificationService.createOnce(
        wl.getOwner(),
        "RESERVATION",
        msg,
        "reserve:" + item.getId()
    );

    return "redirect:/wishlists/" + wishlistId;
  }

  @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/unreserve")
  public String unreserve(@PathVariable Long wishlistId,
                          @PathVariable Long wishId,
                          Principal principal) {

    User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

    WishItem item = wishItemRepository.findById(wishId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // Снять бронь может только тот, кто бронировал
    if (item.getReservedBy() == null || !item.getReservedBy().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    item.setReservedBy(null);
    item.setReservedAt(null);
    wishItemRepository.save(item);

    return "redirect:/wishlists/" + wishlistId;
  }
}
