package com.wishers.controller;

import com.wishers.domain.entity.*;
import com.wishers.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WishlistPageController {

    private final WishListRepository wishListRepository;
    private final WishItemRepository wishItemRepository;
    private final WishFundRepository wishFundRepository;
    private final WishContributionRepository wishContributionRepository;
    private final UserRepository userRepository;

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
        User me = userRepository.findByEmail(principal.getName()).orElseThrow();

        WishList wl = wishListRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!wl.getOwner().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<WishItem> items = wishItemRepository.findAllByWishListOrderByIdDesc(wl);

        Map<Long, WishFund> fundsByWishId = new HashMap<>();
        Map<Long, Long> raisedByWishId = new HashMap<>();

        for (WishItem it : items) {
            wishFundRepository.findByWishItem(it).ifPresent(fund -> {
                fundsByWishId.put(it.getId(), fund);
                long raised = wishContributionRepository.sumByFund(fund);
                raisedByWishId.put(it.getId(), raised);
            });
        }

        model.addAttribute("wishlist", wl);
        model.addAttribute("items", items);
        model.addAttribute("fundsByWishId", fundsByWishId);
        model.addAttribute("raisedByWishId", raisedByWishId);
        return "wishlists/show";
    }

    @PostMapping("/wishlists/{id}/wishes")
    public String createWish(@PathVariable Long id,
                             Principal principal,
                             @RequestParam String title,
                             @RequestParam(required = false) String link,
                             @RequestParam(required = false) String price,
                             @RequestParam(required = false) String note) {

        User me = userRepository.findByEmail(principal.getName()).orElseThrow();

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

        wishItemRepository.save(item);

        return "redirect:/wishlists/" + id;
    }
}
