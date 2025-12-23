package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishItem;
import com.wishers.repo.FriendshipRepository;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
public class FeedController {

  private final UserRepository userRepository;
  private final FriendshipRepository friendshipRepository;
  private final WishItemRepository wishItemRepository;

  public FeedController(UserRepository userRepository,
                        FriendshipRepository friendshipRepository,
                        WishItemRepository wishItemRepository) {
    this.userRepository = userRepository;
    this.friendshipRepository = friendshipRepository;
    this.wishItemRepository = wishItemRepository;
  }

  private User currentUser(Principal principal) {
    if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    String login = principal.getName();
    return userRepository.findByEmailIgnoreCase(login)
        .or(() -> userRepository.findByNicknameIgnoreCase(login))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  @GetMapping("/feed")
  public String feed(Principal principal, Model model) {
    User me = currentUser(principal);

    List<Long> friendIds = friendshipRepository.findFriendIdsOf(me.getId());
    List<WishItem> items = friendIds.isEmpty()
        ? List.of()
        : wishItemRepository.findRecentByOwnerIds(friendIds, PageRequest.of(0, 50));

    model.addAttribute("items", items);
    model.addAttribute("friendsCount", friendIds.size());
    return "feed/index";
  }
}
