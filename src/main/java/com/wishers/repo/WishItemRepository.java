package com.wishers.repo;

import com.wishers.domain.entity.WishItem;
import com.wishers.domain.entity.WishList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishItemRepository extends JpaRepository<WishItem, Long> {

  @Query("""
    select wi from WishItem wi
      left join fetch wi.reservedBy
    where wi.wishList = :wishList
    order by wi.id desc
  """)
  List<WishItem> findAllByWishListOrderByIdDesc(@Param("wishList") WishList wishList);

  void deleteByWishList(WishList wishList);

  // Лента: последние подарки друзей, подгружаем wishList + owner, сортируем по ID (самые новые сверху)
  @EntityGraph(attributePaths = {"wishList", "wishList.owner"})
  @Query("""
    select wi from WishItem wi
      join wi.wishList wl
      join wl.owner o
    where o.id in :ownerIds
    order by wi.id desc
  """)
  List<WishItem> findRecentByOwnerIds(@Param("ownerIds") List<Long> ownerIds, Pageable pageable);
}
