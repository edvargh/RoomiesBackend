package com.roomies.dto.shoppingitem;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ShoppingItemIdListRequestDto {

  @NotEmpty
  private List<Long> ids;

  public List<Long> getIds() { return ids; }
  public void setIds(List<Long> ids) { this.ids = ids; }

}
