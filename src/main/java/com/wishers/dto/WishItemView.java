package com.wishers.dto;

import com.wishers.domain.entity.WishFund;
import com.wishers.domain.entity.WishItem;

public class WishItemView {
  private final WishItem item;
  private final boolean fundEnabled;
  private final long targetRub;
  private final long collectedAmount;
  private final int collectedPercent;

  public WishItemView(WishItem item, WishFund fund, long collectedAmount) {
    this.item = item;
    this.fundEnabled = fund != null && fund.isEnabled();
    this.targetRub = fundEnabled ? fund.getTargetAmountRub() : 0;
    this.collectedAmount = fundEnabled && collectedAmount > 0 ? collectedAmount : 0;

    if (fundEnabled && targetRub > 0) {
      double percent = (this.collectedAmount * 100.0) / targetRub;
      this.collectedPercent = (int) Math.min(100, Math.round(percent));
    } else {
      this.collectedPercent = 0;
    }
  }

  public WishItem getItem() { return item; }
  public boolean isFundEnabled() { return fundEnabled; }
  public long getTargetRub() { return targetRub; }
  public long getCollectedAmount() { return collectedAmount; }
  public int getCollectedPercent() { return collectedPercent; }
}
