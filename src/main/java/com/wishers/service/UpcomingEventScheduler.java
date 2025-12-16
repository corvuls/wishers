package com.wishers.service;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishList;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishListRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class UpcomingEventScheduler {

  private final UserRepository userRepository;
  private final WishListRepository wishListRepository;
  private final NotificationService notificationService;

  private static final int[] DAYS = new int[]{7,3,1};

  public UpcomingEventScheduler(UserRepository userRepository,
                                WishListRepository wishListRepository,
                                NotificationService notificationService) {
    this.userRepository = userRepository;
    this.wishListRepository = wishListRepository;
    this.notificationService = notificationService;
  }

  @Scheduled(cron = "0 5 0 * * *")
  public void runDaily() {
    LocalDate today = LocalDate.now();
    List<User> users = userRepository.findAll();
    for (User u : users) {
      for (int d : DAYS) {
        LocalDate target = today.plusDays(d);
        List<WishList> lists = wishListRepository.findByOwnerAndEventDate(u, target);
        for (WishList w : lists) {
          String key = "upcoming:" + u.getId() + ":" + w.getId() + ":" + target + ":" + d;
          String msg = "Скоро событие: " + w.getTitle() + " через " + d + " дн.";
          notificationService.createOnce(u, "UPCOMING_EVENT", msg, key);
        }
      }
    }
  }
}
