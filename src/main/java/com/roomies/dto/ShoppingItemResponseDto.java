package com.roomies.dto;

import com.roomies.entity.ShoppingItem;
import java.time.LocalDateTime;

public class ShoppingItemResponseDto {

  private Long itemId;
  private String name;
  private String quantity;
  private boolean purchased;
  private String addedBy;
  private String purchasedBy;
  private LocalDateTime addedAt;
  private LocalDateTime purchasedAt;

  public ShoppingItemResponseDto() {
    // Default constructor
  }

  public static ShoppingItemResponseDto fromEntity(ShoppingItem item) {
    ShoppingItemResponseDto dto = new ShoppingItemResponseDto();
    dto.setItemId(item.getItemId());
    dto.setName(item.getName());
    dto.setQuantity(item.getQuantity());
    dto.setPurchased(item.isPurchased());
    dto.setAddedBy(item.getAddedBy().getDisplayName());
    dto.setPurchasedBy(item.getPurchasedBy() != null ? item.getPurchasedBy().getDisplayName() : null);
    dto.setAddedAt(item.getAddedAt());
    dto.setPurchasedAt(item.getPurchasedAt());
    return dto;
  }

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getQuantity() {
    return quantity;
  }

  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  public boolean isPurchased() {
    return purchased;
  }

  public void setPurchased(boolean purchased) {
    this.purchased = purchased;
  }

  public String getAddedBy() {
    return addedBy;
  }

  public void setAddedBy(String addedBy) {
    this.addedBy = addedBy;
  }

  public String getPurchasedBy() {
    return purchasedBy;
  }

  public void setPurchasedBy(String purchasedBy) {
    this.purchasedBy = purchasedBy;
  }

  public LocalDateTime getAddedAt() {
    return addedAt;
  }

  public void setAddedAt(LocalDateTime addedAt) {
    this.addedAt = addedAt;
  }

  public LocalDateTime getPurchasedAt() {
    return purchasedAt;
  }

  public void setPurchasedAt(LocalDateTime purchasedAt) {
    this.purchasedAt = purchasedAt;
  }
}
