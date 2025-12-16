package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishContribution;
import com.wishers.domain.entity.WishFund;
import com.wishers.domain.entity.WishItem;
import com.wishers.domain.entity.WishList;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishContributionRepository;
import com.wishers.repo.WishFundRepository;
import com.wishers.repo.WishItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class WishFundController {

    private final WishItemRepository wishItemRepository;
    private final WishFundRepository wishFundRepository;
    private final WishContributionRepository wishContributionRepository;
    private final UserRepository userRepository;

    public WishFundController(
            WishItemRepository wishItemRepository,
            WishFundRepository wishFundRepository,
            WishContributionRepository wishContributionRepository,
            UserRepository userRepository
    ) {
        this.wishItemRepository = wishItemRepository;
        this.wishFundRepository = wishFundRepository;
        this.wishContributionRepository = wishContributionRepository;
        this.userRepository = userRepository;
    }

    // Если кто-то открыл URL руками в браузере (GET) — просто вернёмся в вишлист
    @Transactional
    @GetMapping("/wishes/{wishId}/fund/enable")
    public String enableFundGet(@PathVariable Long wishId) {
        WishItem wish = wishItemRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        WishList wl = wish.getWishList();
        if (wl == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return "redirect:/wishlists/" + wl.getId();
    }

    // Включить копилку (POST из формы)
    @Transactional
    @PostMapping("/wishes/{wishId}/fund/enable")
    public String enableFund(@PathVariable Long wishId,
                             @RequestParam long targetRub,
                             Principal principal) {

        if (targetRub <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "targetRub must be > 0");
        }

        User me = userRepository.findByEmail(principal.getName()).orElseThrow();

        WishItem wish = wishItemRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        WishList wl = wish.getWishList();
        if (wl == null || wl.getOwner() == null || !wl.getOwner().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        WishFund fund = wishFundRepository.findByWishItem(wish).orElseGet(() -> {
            WishFund f = new WishFund();
            f.setWishItem(wish);
            return f;
        });

        fund.setTargetAmountRub(targetRub);
        fund.setEnabled(true);
        wishFundRepository.save(fund);

        return "redirect:/wishlists/" + wl.getId();
    }

    // GET на contribute тоже редиректим, чтобы не было 405
    @Transactional
    @GetMapping("/wishes/{wishId}/fund/contribute")
    public String contributeGet(@PathVariable Long wishId) {
        WishItem wish = wishItemRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        WishList wl = wish.getWishList();
        if (wl == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return "redirect:/wishlists/" + wl.getId();
    }

    // Скинуться в копилку (POST из формы)
    @Transactional
    @PostMapping("/wishes/{wishId}/fund/contribute")
    public String contribute(@PathVariable Long wishId,
                             @RequestParam long amountRub,
                             @RequestParam(required = false) String message,
                             Principal principal) {

        if (amountRub <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amountRub must be > 0");
        }

        User me = userRepository.findByEmail(principal.getName()).orElseThrow();

        WishItem wish = wishItemRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        WishList wl = wish.getWishList();
        if (wl == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        WishFund fund = wishFundRepository.findByWishItem(wish)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fund not enabled"));

        if (!fund.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fund disabled");
        }

        WishContribution c = new WishContribution();
        c.setFund(fund);
        c.setContributor(me);
        c.setAmountRub(amountRub);
        c.setMessage(message == null ? null : message.trim());

        wishContributionRepository.save(c);

        return "redirect:/wishlists/" + wl.getId();
    }
}
