set -euo pipefail

# 1) Friendship entity + repo
mkdir -p src/main/java/com/wishers/domain/entity
cat > src/main/java/com/wishers/domain/entity/Friendship.java <<'EOF'
package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="friendships",
    uniqueConstraints = @UniqueConstraint(name="uk_friendships_pair", columnNames={"user_id","friend_id"})
)
public class Friendship {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="user_id", nullable=false)
  private User user;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="friend_id", nullable=false)
  private User friend;

  @Column(nullable=false)
  private Instant createdAt = Instant.now();

  public Friendship() {}
  public Friendship(User user, User friend) {
    this.user = user;
    this.friend = friend;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public User getFriend() { return friend; }
  public void setFriend(User friend) { this.friend = friend; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
EOF

mkdir -p src/main/java/com/wishers/repo
cat > src/main/java/com/wishers/repo/FriendshipRepository.java <<'EOF'
package com.wishers.repo;

import com.wishers.domain.entity.Friendship;
import com.wishers.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
  boolean existsByUserAndFriend(User user, User friend);

  @Query("select f.friend from Friendship f where f.user = :user order by lower(f.friend.nickname)")
  List<User> findFriendsOf(@Param("user") User user);
}
EOF

# 2) Services
mkdir -p src/main/java/com/wishers/service
cat > src/main/java/com/wishers/service/FriendService.java <<'EOF'
package com.wishers.service;

import com.wishers.domain.entity.Friendship;
import com.wishers.domain.entity.User;
import com.wishers.repo.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendService {
  private final FriendshipRepository friendshipRepository;

  public FriendService(FriendshipRepository friendshipRepository) {
    this.friendshipRepository = friendshipRepository;
  }

  public List<User> friendsOf(User user) {
    return friendshipRepository.findFriendsOf(user);
  }

  public boolean areFriends(User a, User b) {
    return friendshipRepository.existsByUserAndFriend(a, b) && friendshipRepository.existsByUserAndFriend(b, a);
  }

  @Transactional
  public void addFriendBothWays(User me, User other) {
    if (me.getId().equals(other.getId())) return;

    if (!friendshipRepository.existsByUserAndFriend(me, other)) {
      friendshipRepository.save(new Friendship(me, other));
    }
    if (!friendshipRepository.existsByUserAndFriend(other, me)) {
      friendshipRepository.save(new Friendship(other, me));
    }
  }
}
EOF

cat > src/main/java/com/wishers/service/NotificationService.java <<'EOF'
package com.wishers.service;

import com.wishers.domain.entity.Notification;
import com.wishers.domain.entity.User;
import com.wishers.repo.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private final NotificationRepository notificationRepository;

  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Transactional
  public void notifyOnce(User user, String type, String message, String dedupeKey) {
    if (dedupeKey != null && notificationRepository.existsByUserAndDedupeKey(user, dedupeKey)) return;

    Notification n = new Notification();
    n.setUser(user);
    n.setType(type);
    n.setMessage(message);
    n.setDedupeKey(dedupeKey);
    notificationRepository.save(n);
  }
}
EOF

# 3) Static handler for uploads
mkdir -p src/main/java/com/wishers/config
cat > src/main/java/com/wishers/config/StaticResourceConfig.java <<'EOF'
package com.wishers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

  @Value("${wishers.upload-dir:./data/uploads}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
    String location = dir.toUri().toString();
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(location)
        .setCachePeriod(3600);
  }
}
EOF

# 4) Update repos
cat > src/main/java/com/wishers/repo/UserRepository.java <<'EOF'
package com.wishers.repo;

import com.wishers.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByNicknameIgnoreCase(String nickname);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNicknameIgnoreCase(String nickname);

    List<User> findTop20ByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nickname, String email);
}
EOF

# 5) Add missing method to NotificationRepository (safe overwrite)
cat > src/main/java/com/wishers/repo/NotificationRepository.java <<'EOF'
package com.wishers.repo;

import com.wishers.domain.entity.Notification;
import com.wishers.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndDedupeKey(User user, String dedupeKey);
}
EOF

# 6) Ensure upload props
if ! grep -q "wishers.upload-dir" src/main/resources/application.properties; then
  cat >> src/main/resources/application.properties <<'EOF'

# Uploads
wishers.upload-dir=./data/uploads
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
EOF
fi

mkdir -p data/uploads

echo "PATCH APPLIED OK"
