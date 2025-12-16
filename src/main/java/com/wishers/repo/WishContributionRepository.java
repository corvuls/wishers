package com.wishers.repo;

import com.wishers.domain.entity.WishContribution;
import com.wishers.domain.entity.WishFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WishContributionRepository extends JpaRepository<WishContribution, Long> {

    @Query("select coalesce(sum(c.amountRub), 0) from WishContribution c where c.fund = ?1")
    long sumByFund(WishFund fund);
}
