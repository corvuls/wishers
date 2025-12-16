package com.wishers.repo;

import com.wishers.domain.entity.Notification;
import com.wishers.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  boolean existsByUserAndDedupeKey(User user, String dedupeKey);
  List<Notification> findTop50ByUserOrderByCreatedAtDesc(User user);
}
