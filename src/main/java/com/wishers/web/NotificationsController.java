package com.wishers.web;

import com.wishers.repo.NotificationRepository;
import com.wishers.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationsController {
  private final CurrentUserService currentUserService;
  private final NotificationRepository notificationRepository;

  public NotificationsController(CurrentUserService currentUserService,
                                 NotificationRepository notificationRepository) {
    this.currentUserService = currentUserService;
    this.notificationRepository = notificationRepository;
  }

  @GetMapping("/notifications")
  public String page(Model model) {
    var u = currentUserService.requireUser();
    model.addAttribute("items", notificationRepository.findTop50ByUserOrderByCreatedAtDesc(u));
    return "notifications/index";
  }
}
