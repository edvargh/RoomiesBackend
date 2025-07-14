package com.roomies.repository;

import com.roomies.entity.ShoppingItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for handling shopping item related requests.
 */
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
  
  /**
   * Finds all shopping items associated with a specific household.
   *
   * @param householdId the ID of the household
   * @return a list of shopping items for the specified household
   */
  List<ShoppingItem> findByHousehold_HouseholdId(Long householdId);
}
