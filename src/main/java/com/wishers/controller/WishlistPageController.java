package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishFund;
import com.wishers.domain.entity.WishItem;
import com.wishers.domain.entity.WishList;
import com.wishers.dto.WishItemView;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishContributionRepository;
import com.wishers.repo.WishFundRepository;
import com.wishers.repo.WishItemRepository;
import com.wishers.repo.WishListRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class WishlistPageController {

  private final WishListRepository wishListRepository;
  private final WishItemRepository wishItemRepository;
  private final WishFundRepository wishFundRepository;
  private final WishContributionRepository wishContributionRepository;
  private final UserRepository userRepository;

  @Value("${wishers.upload-dir:./data/uploads}")
  private String uploadDir;

  public WishlistPageController(WishListRepository wishListRepository,
                                WishItemRepository wishItemRepository,
                                WishFundRepository wishFundRepository,
                                WishContributionRepository wishContributionRepository,
                                UserRepository userRepository) {
    this.wishListRepository = wishListRepository;
    this.wishItemRepository = wishItemRepository;
    this.wishFundRepository = wishFundRepository;
    this.wishContributionRepository = wishContributionRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/wishlists/{id}")
  public String show(@PathVariable Long id, Principal principal, Model model) {
    User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

    WishList wl = wishListRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    boolean isOwner = wl.getOwner().getId().equals(me.getId());

    List<WishItem> items = wishItemRepository.findAllByWishListOrderByIdDesc(wl);

    List<WishItemView> itemViews = items.stream()
        .map(item -> {
          WishFund fund = wishFundRepository.findByWishItem(item).orElse(null);
          long raised = fund == null ? 0 : wishContributionRepository.sumByFund(fund);
          return new WishItemView(item, fund, raised);
        })
        .toList();

    model.addAttribute("wishlist", wl);
    model.addAttribute("items", items);
    model.addAttribute("itemViews", itemViews);
    model.addAttribute("meId", me.getId());
    model.addAttribute("isOwner", isOwner);

    return "wishlists/show";
  }

  // Добавление подарка в уже существующий вишлист (только владелец) + фото
  @PostMapping("/wishlists/{id}/wishes")
  public String createWish(@PathVariable Long id,
                           Principal principal,
                           @RequestParam String title,
                           @RequestParam(required = false) String link,
                           @RequestParam(required = false) String price,
                           @RequestParam(required = false) String note,
                           @RequestParam(required = false, name="image") MultipartFile image) {

    User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

    WishList wl = wishListRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!wl.getOwner().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    WishItem item = new WishItem();
    item.setWishList(wl);
    item.setTitle(title.trim());
    item.setLink(link == null ? null : link.trim());
    item.setPrice(price == null ? null : price.trim());
    item.setNote(note == null ? null : note.trim());

    if (image != null && !image.isEmpty()) {
      item.setImagePath("/uploads/" + saveImage(image));
    }

    wishItemRepository.save(item);
    return "redirect:/wishlists/" + id;
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
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось сохранить фото");
    }
  }
}
