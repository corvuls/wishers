package com.wishers.repo;

import com.wishers.domain.entity.User;
import com.wishers.domain.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    List<WishList> findAllByOwner(User owner);
    List<WishList> findAllByOwnerOrderByIdDesc(User owner);

    List<WishList> findByOwnerAndEventDate(User owner, LocalDate eventDate);
}
