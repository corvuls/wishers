package com.wishers.service;

import com.wishers.domain.entity.Notification;
import com.wishers.domain.entity.User;
import com.wishers.repo.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  private final NotificationRepository repo;

  public NotificationService(NotificationRepository repo) {
    this.repo = repo;
  }

  public void createOnce(User user, String type, String message, String dedupeKey) {
    if (dedupeKey != null && repo.existsByUserAndDedupeKey(user, dedupeKey)) return;
    Notification n = new Notification();
    n.setUser(user);
    n.setType(type);
    n.setMessage(message);
    n.setDedupeKey(dedupeKey);
    repo.save(n);
  }
}
