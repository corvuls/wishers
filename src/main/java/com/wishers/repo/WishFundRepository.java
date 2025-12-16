package com.wishers.repo;

import com.wishers.domain.entity.WishFund;
import com.wishers.domain.entity.WishItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishFundRepository extends JpaRepository<WishFund, Long> {
    Optional<WishFund> findByWishItem(WishItem wishItem);
}
