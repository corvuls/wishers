package com.wishers.repo;

import com.wishers.domain.entity.Friendship;
import com.wishers.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

  @Query("""
    select f from Friendship f
    where (f.userA.id = :a and f.userB.id = :b) or (f.userA.id = :b and f.userB.id = :a)
  """)
  Optional<Friendship> findBetween(@Param("a") Long a, @Param("b") Long b);

  @Query("""
    select (count(f) > 0) from Friendship f
    where (f.userA.id = :a and f.userB.id = :b) or (f.userA.id = :b and f.userB.id = :a)
  """)
  boolean existsBetween(@Param("a") Long a, @Param("b") Long b);

  // Совместимость со старым кодом
  default boolean existsByUserAndFriend(User user, User friend) {
    return existsBetween(user.getId(), friend.getId());
  }

  // Список id друзей (без CASE по entity — чтобы Hibernate не падал)
  @Query("""
    select case when f.userA.id = :me then f.userB.id else f.userA.id end
    from Friendship f
    where f.userA.id = :me or f.userB.id = :me
    order by f.createdAt desc
  """)
  List<Long> findFriendIdsOf(@Param("me") Long me);
}
