set -euo pipefail

# -----------------------------
# 1) Friendship entity + repo + service
# -----------------------------
mkdir -p src/main/java/com/wishers/domain/entity
cat > src/main/java/com/wishers/domain/entity/Friendship.java <<'EOF'
package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name="friendships",
    uniqueConstraints = @UniqueConstraint(
        name="uk_friendships_pair",
        columnNames={"user_id","friend_id"}
    )
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
    return friendshipRepository.existsByUserAndFriend(a, b)
        && friendshipRepository.existsByUserAndFriend(b, a);
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

# -----------------------------
# 2) Static resource handler for uploads
# -----------------------------
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
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(dir.toUri().toString())
        .setCachePeriod(3600);
  }
}
EOF

# -----------------------------
# 3) Add image + reservation fields to WishItem
# -----------------------------
cat > src/main/java/com/wishers/domain/entity/WishItem.java <<'EOF'
package com.wishers.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "wish_items")
public class WishItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private WishList wishList;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String link;

    @Column(length = 64)
    private String price;

    @Column(length = 2000)
    private String note;

    // Uploaded image served via /uploads/**
    @Column(length = 300)
    private String imagePath;

    // Reservation
    @ManyToOne(fetch = FetchType.LAZY)
    private User reservedBy;

    private Instant reservedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }

    public WishList getWishList() { return wishList; }
    public void setWishList(WishList wishList) { this.wishList = wishList; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public User getReservedBy() { return reservedBy; }
    public void setReservedBy(User reservedBy) { this.reservedBy = reservedBy; }

    public Instant getReservedAt() { return reservedAt; }
    public void setReservedAt(Instant reservedAt) { this.reservedAt = reservedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
EOF

# -----------------------------
# 4) Update WishItemRepository to fetch reservedBy
# -----------------------------
cat > src/main/java/com/wishers/repo/WishItemRepository.java <<'EOF'
package com.wishers.repo;

import com.wishers.domain.entity.WishItem;
import com.wishers.domain.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishItemRepository extends JpaRepository<WishItem, Long> {

    @Query("select wi from WishItem wi left join fetch wi.reservedBy where wi.wishList = :wishList order by wi.id desc")
    List<WishItem> findAllByWishListOrderByIdDesc(@Param("wishList") WishList wishList);
}
EOF

# -----------------------------
# 5) Extend UserRepository with search by nickname/email (for friends)
# -----------------------------
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

# -----------------------------
# 6) NotificationService: keep createOnce and add alias notifyOnce (compat)
# -----------------------------
cat > src/main/java/com/wishers/service/NotificationService.java <<'EOF'
package com.wishers.service;

import com.wishers.domain.entity.Notification;
import com.wishers.domain.entity.User;
import com.wishers.repo.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private final NotificationRepository repo;

  public NotificationService(NotificationRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public void createOnce(User user, String type, String message, String dedupeKey) {
    if (dedupeKey != null && repo.existsByUserAndDedupeKey(user, dedupeKey)) return;
    Notification n = new Notification();
    n.setUser(user);
    n.setType(type);
    n.setMessage(message);
    n.setDedupeKey(dedupeKey);
    repo.save(n);
  }

  @Transactional
  public void notifyOnce(User user, String type, String message, String dedupeKey) {
    createOnce(user, type, message, dedupeKey);
  }
}
EOF

# -----------------------------
# 7) WishlistPageController: allow friends view + upload + reserve/unreserve + notifications
# -----------------------------
cat > src/main/java/com/wishers/controller/WishlistPageController.java <<'EOF'
package com.wishers.controller;

import com.wishers.domain.entity.*;
import com.wishers.repo.*;
import com.wishers.service.FriendService;
import com.wishers.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class WishlistPageController {

    private final WishListRepository wishListRepository;
    private final WishItemRepository wishItemRepository;
    private final WishFundRepository wishFundRepository;
    private final WishContributionRepository wishContributionRepository;
    private final UserRepository userRepository;
    private final FriendService friendService;
    private final NotificationService notificationService;

    @Value("${wishers.upload-dir:./data/uploads}")
    private String uploadDir;

    public WishlistPageController(WishListRepository wishListRepository,
                                  WishItemRepository wishItemRepository,
                                  WishFundRepository wishFundRepository,
                                  WishContributionRepository wishContributionRepository,
                                  UserRepository userRepository,
                                  FriendService friendService,
                                  NotificationService notificationService) {
        this.wishListRepository = wishListRepository;
        this.wishItemRepository = wishItemRepository;
        this.wishFundRepository = wishFundRepository;
        this.wishContributionRepository = wishContributionRepository;
        this.userRepository = userRepository;
        this.friendService = friendService;
        this.notificationService = notificationService;
    }

    @GetMapping("/wishlists/{id}")
    public String show(@PathVariable Long id, Principal principal, Model model) {
        User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

        WishList wl = wishListRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isOwner = wl.getOwner().getId().equals(me.getId());
        if (!isOwner) {
            if (!friendService.areFriends(me, wl.getOwner())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
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

        model.addAttribute("meId", me.getId());
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("ownerNickname", wl.getOwner().getNickname());
        return "wishlists/show";
    }

    @PostMapping("/wishlists/{id}/wishes")
    public String createWish(@PathVariable Long id,
                             Principal principal,
                             @RequestParam String title,
                             @RequestParam(required = false) String link,
                             @RequestParam(required = false) String price,
                             @RequestParam(required = false) String note,
                             @RequestParam(required = false, name = "image") MultipartFile image) {

        User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

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

        if (image != null && !image.isEmpty()) {
            String saved = saveImage(image);
            item.setImagePath("/uploads/" + saved);
        }

        wishItemRepository.save(item);
        return "redirect:/wishlists/" + id;
    }

    @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/reserve")
    public String reserve(@PathVariable Long wishlistId,
                          @PathVariable Long wishId,
                          Principal principal) {

        User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

        WishList wl = wishListRepository.findById(wishlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (wl.getOwner().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!friendService.areFriends(me, wl.getOwner())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        WishItem item = wishItemRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!item.getWishList().getId().equals(wl.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (item.getReservedBy() != null && !item.getReservedBy().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        item.setReservedBy(me);
        item.setReservedAt(Instant.now());
        wishItemRepository.save(item);

        String msg = me.getNickname() + " забронировал(а) подарок: " + item.getTitle();
        notificationService.createOnce(wl.getOwner(), "RESERVATION", msg,
                "reserve:" + item.getId() + ":" + me.getId());

        return "redirect:/wishlists/" + wishlistId;
    }

    @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/unreserve")
    public String unreserve(@PathVariable Long wishlistId,
                            @PathVariable Long wishId,
                            Principal principal) {

        User me = userRepository.findByEmailIgnoreCase(principal.getName()).orElseThrow();

        WishItem item = wishItemRepository.findById(wishId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (item.getReservedBy() == null || !item.getReservedBy().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        item.setReservedBy(null);
        item.setReservedAt(null);
        wishItemRepository.save(item);

        return "redirect:/wishlists/" + wishlistId;
    }

    private String saveImage(MultipartFile file) {
        try {
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null) {
                int i = original.lastIndexOf('.');
                if (i >= 0 && i < original.length() - 1) {
                    String candidate = original.substring(i).toLowerCase();
                    if (candidate.equals(".png") || candidate.equals(".jpg") || candidate.equals(".jpeg") || candidate.equals(".webp")) {
                        ext = candidate;
                    }
                }
            }

            String name = UUID.randomUUID() + ext;
            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);

            Path dst = dir.resolve(name).normalize();
            Files.copy(file.getInputStream(), dst);
            return name;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось сохранить фото");
        }
    }
}
EOF

# -----------------------------
# 8) FriendsController: list + search + add + friend profile with wishlists
# -----------------------------
cat > src/main/java/com/wishers/controller/FriendsController.java <<'EOF'
package com.wishers.controller;

import com.wishers.domain.entity.User;
import com.wishers.repo.UserRepository;
import com.wishers.repo.WishListRepository;
import com.wishers.service.CurrentUserService;
import com.wishers.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class FriendsController {

  private final CurrentUserService currentUserService;
  private final FriendService friendService;
  private final UserRepository userRepository;
  private final WishListRepository wishListRepository;

  public FriendsController(CurrentUserService currentUserService,
                           FriendService friendService,
                           UserRepository userRepository,
                           WishListRepository wishListRepository) {
    this.currentUserService = currentUserService;
    this.friendService = friendService;
    this.userRepository = userRepository;
    this.wishListRepository = wishListRepository;
  }

  @GetMapping("/friends")
  public String friends(@RequestParam(value = "q", required = false) String q, Model model) {
    User me = currentUserService.requireUser();

    model.addAttribute("friends", friendService.friendsOf(me));
    String query = q == null ? "" : q.trim();
    model.addAttribute("q", query);

    if (!query.isBlank()) {
      List<User> results = userRepository
          .findTop20ByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
          .stream()
          .filter(u -> !u.getId().equals(me.getId()))
          .filter(u -> !friendService.areFriends(me, u))
          .toList();
      model.addAttribute("results", results);
    }

    return "friends/index";
  }

  @PostMapping("/friends/add")
  public String add(@RequestParam("userId") Long userId) {
    User me = currentUserService.requireUser();
    User other = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    friendService.addFriendBothWays(me, other);
    return "redirect:/friends";
  }

  @GetMapping("/friends/{nickname}")
  public String friend(@PathVariable String nickname, Model model) {
    User me = currentUserService.requireUser();
    User other = userRepository.findByNicknameIgnoreCase(nickname)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    if (!friendService.areFriends(me, other)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    model.addAttribute("friend", other);
    model.addAttribute("wishlists", wishListRepository.findAllByOwner(other));
    return "friends/show";
  }
}
EOF

# -----------------------------
# 9) Templates: friends + wishlists/show (clean & pretty)
# -----------------------------
mkdir -p src/main/resources/templates/friends
cat > src/main/resources/templates/friends/index.html <<'EOF'
<!doctype html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="app_layout :: head('Друзья')"></head>

<body th:replace="app_layout :: page(~{::main}, 'Друзья', 'friends')">
<main>
  <div class="rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden">
    <div class="text-2xl font-semibold">Друзья</div>
    <div class="text-white/60 mt-1">Найди по нику или email и добавь. После этого увидишь их вишлисты.</div>

    <form class="mt-4 flex gap-2" action="/friends/search" method="get">
      <input name="q" th:value="${q}"
             class="flex-1 rounded-2xl bg-black/30 border border-white/10 px-4 py-3 outline-none focus:border-white/30"
             placeholder="Ник или email"/>
      <button type="submit"
              class="px-4 py-3 rounded-2xl bg-white text-black font-semibold hover:bg-white/90">
        Найти
      </button>
    </form>
  </div>

  <div class="mt-4 rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden">
    <div class="text-lg font-semibold">Мои друзья</div>

    <div class="mt-3 space-y-3">
      <div th:if="${friends == null or #lists.isEmpty(friends)}" class="text-white/60">
        Пока нет друзей. Найди пользователя сверху и добавь.
      </div>

      <div th:each="u : ${friends}" class="rounded-2xl bg-white/5 border border-white/10 p-4 overflow-hidden flex items-center justify-between gap-3">
        <div>
          <div class="font-semibold" th:text="${u.nickname}">nickname</div>
          <div class="text-white/60 text-sm" th:text="${u.email}">email</div>
        </div>

        <a th:href="@{/friends/{nick}(nick=${u.nickname})}"
           class="px-4 py-2 rounded-2xl bg-white text-black font-semibold hover:bg-white/90">
          Вишлисты
        </a>
      </div>
    </div>
  </div>

  <div class="mt-4 rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden"
       th:if="${results != null}">
    <div class="text-lg font-semibold">Результаты</div>

    <div class="mt-3 space-y-3">
      <div th:if="${#lists.isEmpty(results)}" class="text-white/60">Никого не нашли.</div>

      <div th:each="u : ${results}" class="rounded-2xl bg-white/5 border border-white/10 p-4 overflow-hidden flex items-center justify-between gap-3">
        <div>
          <div class="font-semibold" th:text="${u.nickname}">nickname</div>
          <div class="text-white/60 text-sm" th:text="${u.email}">email</div>
        </div>

        <form method="post" action="/friends/add">
          <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
          <input type="hidden" name="userId" th:value="${u.id}"/>
          <button type="submit"
                  class="px-4 py-2 rounded-2xl bg-white text-black font-semibold hover:bg-white/90">
            Добавить
          </button>
        </form>
      </div>
    </div>
  </div>
</main>
</body>
</html>
EOF

cat > src/main/resources/templates/friends/show.html <<'EOF'
<!doctype html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="app_layout :: head('Профиль друга')"></head>

<body th:replace="app_layout :: page(~{::main}, 'Профиль', 'friends')">
<main>
  <div class="rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden">
    <div class="text-2xl font-semibold" th:text="${friend.nickname}">friend</div>
    <div class="text-white/60 mt-1" th:text="${friend.email}">email</div>

    <div class="mt-4">
      <a href="/friends" class="px-4 py-2 rounded-2xl bg-white/10 border border-white/10 hover:bg-white/15">
        Назад
      </a>
    </div>
  </div>

  <div class="mt-4 rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden">
    <div class="text-lg font-semibold">Вишлисты</div>

    <div class="mt-3 space-y-3">
      <div th:if="${wishlists == null or #lists.isEmpty(wishlists)}" class="text-white/60">
        У пользователя пока нет вишлистов.
      </div>

      <div th:each="w : ${wishlists}" class="rounded-2xl bg-white/5 border border-white/10 p-4 overflow-hidden">
        <div class="text-lg font-semibold" th:text="${w.title}">Список</div>
        <div class="text-white/60 mt-1" th:text="${w.description} ?: ''"></div>

        <div class="mt-4">
          <a th:href="@{/wishlists/{id}(id=${w.id})}"
             class="inline-block px-4 py-2 rounded-2xl bg-white text-black font-semibold hover:bg-white/90">
            Открыть
          </a>
        </div>
      </div>
    </div>
  </div>
</main>
</body>
</html>
EOF

cat > src/main/resources/templates/wishlists/show.html <<'EOF'
<!doctype html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="app_layout :: head('Вишлист')"></head>

<body th:replace="app_layout :: page(~{::main}, 'Вишлист', 'wishlists')">
<main>
  <div class="rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden">
    <div class="text-2xl font-semibold" th:text="${wishlist.title}">Вишлист</div>
    <div class="text-white/60 mt-1" th:text="${wishlist.description} ?: ''"></div>

    <div class="mt-4 flex gap-2 flex-wrap">
      <a href="/wishlists" class="px-4 py-2 rounded-2xl bg-white/10 border border-white/10 hover:bg-white/15">Назад</a>
      <a th:if="${!isOwner}" href="/friends" class="px-4 py-2 rounded-2xl bg-white/10 border border-white/10 hover:bg-white/15">Друзья</a>
    </div>
  </div>

  <!-- add wish only for owner -->
  <div class="mt-4 rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden" th:if="${isOwner}">
    <div class="text-lg font-semibold">Добавить желание</div>

    <form class="mt-4 grid gap-2"
          th:action="@{/wishlists/{id}/wishes(id=${wishlist.id})}"
          method="post" enctype="multipart/form-data">
      <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

      <input name="title" required maxlength="200"
             class="w-full rounded-2xl bg-black/30 border border-white/10 px-4 py-3 outline-none focus:border-white/30"
             placeholder="Название (например: AirPods)"/>

      <input name="link" type="url"
             class="w-full rounded-2xl bg-black/30 border border-white/10 px-4 py-3 outline-none focus:border-white/30"
             placeholder="Ссылка (необязательно)"/>

      <input name="price"
             class="w-full rounded-2xl bg-black/30 border border-white/10 px-4 py-3 outline-none focus:border-white/30"
             placeholder="Цена (необязательно)"/>

      <textarea name="note" rows="3"
                class="w-full rounded-2xl bg-black/30 border border-white/10 px-4 py-3 outline-none focus:border-white/30"
                placeholder="Комментарий (цвет/размер и т.п., необязательно)"></textarea>

      <div>
        <label class="block text-sm text-white/70 mb-2">Фото (необязательно)</label>
        <input name="image" type="file" accept="image/*"
               class="w-full rounded-2xl bg-black/30 border border-white/10 px-4 py-3 outline-none focus:border-white/30"/>
      </div>

      <button type="submit"
              class="w-full rounded-2xl bg-white text-black font-semibold py-3 hover:bg-white/90">
        Добавить
      </button>
    </form>
  </div>

  <div class="mt-4 rounded-3xl bg-white/5 border border-white/10 p-4 overflow-hidden">
    <div class="text-lg font-semibold">Желания</div>

    <div class="mt-3 space-y-3">
      <div th:if="${items == null or #lists.isEmpty(items)}" class="text-white/60">
        Пока пусто.
      </div>

      <div th:each="it : ${items}" class="rounded-2xl bg-white/5 border border-white/10 p-4 overflow-hidden">
        <div class="flex items-start justify-between gap-3">
          <div class="text-lg font-semibold" th:text="${it.title}">Название</div>
          <div class="text-white/60" th:text="${it.price != null and !#strings.isEmpty(it.price) ? (it.price + ' ₽') : ''}"></div>
        </div>

        <div class="mt-2" th:if="${it.link != null and !#strings.isEmpty(it.link)}">
          <a class="text-white underline underline-offset-4 break-all" th:href="${it.link}" target="_blank" rel="noopener"
             th:text="${it.link}">link</a>
        </div>

        <div class="mt-2 text-white/70" th:if="${it.note != null and !#strings.isEmpty(it.note)}"
             th:text="${it.note}">note</div>

        <div class="mt-3" th:if="${it.imagePath != null and !#strings.isEmpty(it.imagePath)}">
          <img th:src="${it.imagePath}" alt="Фото"
               class="w-full rounded-2xl border border-white/10 object-cover max-h-72"/>
        </div>

        <!-- reservation -->
        <div class="mt-3 rounded-2xl bg-black/20 border border-white/10 p-3">
          <div class="text-sm text-white/70">Бронь</div>

          <div th:if="${isOwner}">
            <div class="mt-1 text-white/60" th:if="${it.reservedBy == null}">Свободно</div>
            <div class="mt-1 text-white/90" th:if="${it.reservedBy != null}">
              Забронировано: <span class="font-semibold" th:text="${it.reservedBy.nickname}">friend</span>
            </div>
          </div>

          <div th:if="${!isOwner}">
            <div class="mt-1 text-white/60" th:if="${it.reservedBy == null}">
              Свободно — можешь забронировать.
            </div>

            <div class="mt-1 text-white/90" th:if="${it.reservedBy != null and it.reservedBy.id != meId}">
              Уже забронировано: <span class="font-semibold" th:text="${it.reservedBy.nickname}">friend</span>
            </div>

            <div class="mt-1 text-white/90" th:if="${it.reservedBy != null and it.reservedBy.id == meId}">
              Забронировано тобой.
            </div>

            <div class="mt-3 flex gap-2 flex-wrap">
              <form th:if="${it.reservedBy == null}"
                    th:action="@{/wishlists/{wid}/wishes/{id}/reserve(wid=${wishlist.id}, id=${it.id})}"
                    method="post">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                <button type="submit" class="px-4 py-2 rounded-2xl bg-white text-black font-semibold hover:bg-white/90">
                  Забронировать
                </button>
              </form>

              <form th:if="${it.reservedBy != null and it.reservedBy.id == meId}"
                    th:action="@{/wishlists/{wid}/wishes/{id}/unreserve(wid=${wishlist.id}, id=${it.id})}"
                    method="post">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                <button type="submit" class="px-4 py-2 rounded-2xl bg-white/10 border border-white/10 hover:bg-white/15">
                  Снять бронь
                </button>
              </form>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</main>
</body>
</html>
EOF

# -----------------------------
# 10) Add upload props + limits if missing
# -----------------------------
if ! grep -q "wishers.upload-dir" src/main/resources/application.properties; then
  cat >> src/main/resources/application.properties <<'EOF'

# Uploads
wishers.upload-dir=./data/uploads
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
EOF
fi

mkdir -p data/uploads

# -----------------------------
# 11) Remember-me + allow /uploads/**
# -----------------------------
cat > src/main/java/com/wishers/config/SecurityConfig.java <<'EOF'
package com.wishers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth
          .requestMatchers(
              "/login", "/register",
              "/uploads/**",
              "/css/**", "/js/**", "/images/**", "/favicon.ico"
          ).permitAll()
          .requestMatchers("/h2-console/**").permitAll()
          .anyRequest().authenticated()
      )
      .formLogin(form -> form
          .loginPage("/login")
          .loginProcessingUrl("/login")
          .defaultSuccessUrl("/feed", true)
          .failureUrl("/login?error")
          .permitAll()
      )
      .rememberMe(rm -> rm
          .key("wishers-remember-me-key")
          .tokenValiditySeconds(60 * 60 * 24 * 14) // 14 days
      )
      .logout(logout -> logout
          .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
          .logoutSuccessUrl("/login?logout")
          .permitAll()
      );

    http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
    http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    return http.build();
  }
}
EOF

# login checkbox "remember-me"
cat > src/main/resources/templates/auth/login.html <<'EOF'
<!doctype html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{auth_layout :: page(~{::body}, 'Вход')}">
<body>

<section class="space-y-5">
  <div>
    <div class="text-3xl font-semibold tracking-tight">Wishers</div>
    <div class="mt-2 text-sm text-white/60">Войдите в аккаунт.</div>
  </div>

  <form class="space-y-3" method="post" action="/login">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

    <div>
      <label class="block text-sm text-white/70 mb-2">Email</label>
      <input name="username" type="email" required
             class="w-full rounded-2xl bg-white/5 border border-white/10 px-4 py-3 text-base outline-none focus:border-white/30"
             placeholder="name@email.com"/>
    </div>

    <div>
      <label class="block text-sm text-white/70 mb-2">Пароль</label>
      <input name="password" type="password" required
             class="w-full rounded-2xl bg-white/5 border border-white/10 px-4 py-3 text-base outline-none focus:border-white/30"
             placeholder="••••••••"/>
    </div>

    <label class="flex items-center gap-3 text-sm text-white/70 select-none">
      <input type="checkbox" name="remember-me"
             class="h-4 w-4 rounded border-white/20 bg-white/10"/>
      Запомнить меня (14 дней)
    </label>

    <button class="w-full rounded-2xl bg-white text-black px-4 py-3 text-base font-semibold">
      Войти
    </button>

    <div th:if="${param.error}" class="text-sm text-red-300">Неверный email или пароль</div>
    <div th:if="${param.registered}" class="text-sm text-emerald-300">Аккаунт создан. Войди.</div>
    <div th:if="${param.logout}" class="text-sm text-white/60">Вы вышли.</div>
  </form>

  <div class="text-sm text-white/70">
    Нет аккаунта? <a href="/register" class="text-white underline underline-offset-4">Регистрация</a>
  </div>
</section>

</body>
</html>
EOF

echo "PATCH2 OK"
