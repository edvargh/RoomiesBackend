package com.roomies.repository;

import com.roomies.entity.ShoppingItem;
import java.util.Collection;
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

  /**
   * Finds shopping items by household ID and a collection of item IDs.
   *
   * @param householdId the ID of the household
   * @param ids         the collection of item IDs to search for
   * @return a list of shopping items matching the criteria
   */
  List<ShoppingItem> findByHousehold_HouseholdIdAndItemIdIn(Long householdId, Collection<Long> ids);
}
