package com.wishers.service;

import com.wishers.domain.entity.Friendship;
import com.wishers.domain.entity.User;
import com.wishers.repo.FriendshipRepository;
import com.wishers.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {

  private final FriendshipRepository friendshipRepository;
  private final UserRepository userRepository;

  public FriendService(FriendshipRepository friendshipRepository,
                       UserRepository userRepository) {
    this.friendshipRepository = friendshipRepository;
    this.userRepository = userRepository;
  }

  public boolean areFriends(User a, User b) {
    if (a == null || b == null) return false;
    if (a.getId() == null || b.getId() == null) return false;
    if (a.getId().equals(b.getId())) return true;
    return friendshipRepository.existsBetween(a.getId(), b.getId());
  }

  public List<Long> friendIdsOf(User me) {
    return friendshipRepository.findFriendIdsOf(me.getId());
  }

  public List<User> friendsOf(User me) {
    List<Long> ids = friendshipRepository.findFriendIdsOf(me.getId());
    return ids.isEmpty() ? List.of() : userRepository.findAllById(ids);
  }

  public void addFriendship(User a, User b) {
    if (a.getId().equals(b.getId())) return;
    if (friendshipRepository.existsBetween(a.getId(), b.getId())) return;

    Friendship f = new Friendship();
    if (a.getId() < b.getId()) {
      f.setUserA(a);
      f.setUserB(b);
    } else {
      f.setUserA(b);
      f.setUserB(a);
    }
    friendshipRepository.save(f);
  }

  public void removeFriendship(User a, User b) {
    friendshipRepository.findBetween(a.getId(), b.getId()).ifPresent(friendshipRepository::delete);
  }
}
