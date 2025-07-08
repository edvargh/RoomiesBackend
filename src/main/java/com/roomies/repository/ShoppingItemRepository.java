package com.roomies.repository;

import com.roomies.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for handling shopping item related requests.
 */
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
}
