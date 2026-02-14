package com.roomies.dto.shoppingitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShoppingItemRequestDto {

  @NotBlank(message = "Name is required")
  @Size(max = 150)
  private String name;

  @Size(max = 40)
  private String quantity = "1";

  public ShoppingItemRequestDto() {
    // Default constructor
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
}
