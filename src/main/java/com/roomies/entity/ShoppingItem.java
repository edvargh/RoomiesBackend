package com.roomies.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * The ShoppingItem entity represents an item in a collective's shopping list.
 */
@Entity
@Table(name = "shopping_items")
public class ShoppingItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "item_id", nullable = false, updatable = false)
  private Long itemId;

  @ManyToOne(optional = false)
  @JoinColumn(
      name = "collective_id",
      foreignKey = @ForeignKey(name = "fk_shop_collective")
  )
  private Collective collective;

  @ManyToOne(optional = false)
  @JoinColumn(
      name = "added_by",
      foreignKey = @ForeignKey(name = "fk_shop_added_by")
  )
  private User addedBy;

  @ManyToOne
  @JoinColumn(
      name = "purchased_by",
      foreignKey = @ForeignKey(name = "fk_shop_purchased_by")
  )
  private User purchasedBy;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(length = 40)
  private String quantity = "1";

  @Column(nullable = false)
  private boolean purchased = false;

  @Column(name = "added_at",
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP",
      insertable = false,
      updatable = false)
  private LocalDateTime addedAt;

  @Column(name = "purchased_at")
  private LocalDateTime purchasedAt;

  public ShoppingItem() {
    // Default constructor
  }

  public Long getItemId()                  { return itemId; }
  public void setItemId(Long itemId)       { this.itemId = itemId; }

  public Collective getCollective()        { return collective; }
  public void setCollective(Collective c)  { this.collective = c; }

  public User getAddedBy()                 { return addedBy; }
  public void setAddedBy(User u)           { this.addedBy = u; }

  public User getPurchasedBy()             { return purchasedBy; }
  public void setPurchasedBy(User u)       { this.purchasedBy = u; }

  public String getName()                  { return name; }
  public void setName(String name)         { this.name = name; }

  public String getQuantity()              { return quantity; }
  public void setQuantity(String quantity) { this.quantity = quantity; }

  public boolean isPurchased()             { return purchased; }
  public void setPurchased(boolean p)      { this.purchased = p; }

  public LocalDateTime getAddedAt()        { return addedAt; }

  public LocalDateTime getPurchasedAt()    { return purchasedAt; }
  public void setPurchasedAt(LocalDateTime t) { this.purchasedAt = t; }

}
