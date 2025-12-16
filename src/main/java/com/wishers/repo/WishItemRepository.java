package com.wishers.repo;

import com.wishers.domain.entity.WishItem;
import com.wishers.domain.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishItemRepository extends JpaRepository<WishItem, Long> {
    List<WishItem> findAllByWishListOrderByIdDesc(WishList wishList);
}
