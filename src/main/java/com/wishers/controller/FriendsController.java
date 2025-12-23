package com.wishers.controller;

import com.wishers.domain.entity.Friendship;
import com.wishers.domain.entity.User;
import com.wishers.repo.FriendshipRepository;
import com.wishers.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
public class FriendsController {

  private final UserRepository userRepository;
  private final FriendshipRepository friendshipRepository;

  public FriendsController(UserRepository userRepository,
                           FriendshipRepository friendshipRepository) {
    this.userRepository = userRepository;
    this.friendshipRepository = friendshipRepository;
  }

  private User currentUser(Principal principal) {
    if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    String login = principal.getName();
    return userRepository.findByEmailIgnoreCase(login)
        .or(() -> userRepository.findByNicknameIgnoreCase(login))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  @GetMapping("/friends")
  public String friends(Principal principal, Model model) {
    User me = currentUser(principal);

    List<Long> ids = friendshipRepository.findFriendIdsOf(me.getId());
    List<User> friends = ids.isEmpty() ? List.of() : userRepository.findAllById(ids);

    model.addAttribute("friends", friends);
    return "friends/index";
  }

  @PostMapping("/friends/add")
  public String addFriend(Principal principal,
                          @RequestParam("query") String query) {
    User me = currentUser(principal);

    String q = query == null ? "" : query.trim();
    if (q.isEmpty()) return "redirect:/friends";

    User other = userRepository.findByEmailIgnoreCase(q)
        .or(() -> userRepository.findByNicknameIgnoreCase(q))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

    if (other.getId().equals(me.getId())) return "redirect:/friends";
    if (friendshipRepository.existsBetween(me.getId(), other.getId())) return "redirect:/friends";

    Friendship f = new Friendship();
    if (me.getId() < other.getId()) {
      f.setUserA(me);
      f.setUserB(other);
    } else {
      f.setUserA(other);
      f.setUserB(me);
    }
    friendshipRepository.save(f);

    return "redirect:/friends";
  }

  @PostMapping("/friends/remove")
  public String removeFriend(Principal principal,
                             @RequestParam("userId") Long userId) {
    User me = currentUser(principal);
    friendshipRepository.findBetween(me.getId(), userId).ifPresent(friendshipRepository::delete);
    return "redirect:/friends";
  }
}
