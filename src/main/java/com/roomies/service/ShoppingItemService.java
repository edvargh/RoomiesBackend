package com.roomies.service;

import com.roomies.dto.ShoppingItemRequestDto;
import com.roomies.dto.ShoppingItemResponseDto;
import com.roomies.entity.Household;
import com.roomies.entity.ShoppingItem;
import com.roomies.entity.User;
import com.roomies.repository.ShoppingItemRepository;
import com.roomies.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling shopping item related operations.
 */
@Service
public class ShoppingItemService {

  private static final Logger log = LoggerFactory.getLogger(ShoppingItemService.class);
  private final ShoppingItemRepository shoppingItemRepo;
  private final UserRepository userRepo;

  public ShoppingItemService(ShoppingItemRepository shoppingItemRepo, UserRepository userRepo) {
    this.shoppingItemRepo = shoppingItemRepo;
    this.userRepo = userRepo;
  }

  /**
   * Creates a new shopping item for the authenticated user's household.
   *
   * @param dto  the request DTO containing item details
   * @param email the authenticated user's email
   * @throws IllegalArgumentException if the user is not found
   * @throws IllegalStateException if the user is not part of a household
   */
  @Transactional
  public void createItem(ShoppingItemRequestDto dto, String email) {
    User user = getAuthenticatedUser(email);
    Household household = user.getHousehold();
    if (household == null) {
      throw new IllegalStateException("User must be part of a household");
    }

    ShoppingItem item = new ShoppingItem();
    item.setName(dto.getName());
    item.setQuantity(dto.getQuantity());
    item.setHousehold(household);
    item.setAddedBy(user);
    log.debug("Creating shopping item with name: {}", dto.getName());
    ShoppingItem saved = shoppingItemRepo.save(item);
    log.debug("Shopping item created with ID: {}", saved.getItemId());
  }

  /**
   * Retrieves all shopping items for the authenticated user's household.
   *
   * @param email the authenticated user's email
   * @return a list of shopping item response DTOs
   */
  public List<ShoppingItemResponseDto> getItemsForHousehold(String email) {
    User user = getAuthenticatedUser(email);
    Household household = user.getHousehold();
    if (household == null) {
      throw new IllegalStateException("User must be part of a household");
    }
    log.debug("Retrieving shopping items for household ID: {}", household.getHouseholdId());
    return shoppingItemRepo.findByHousehold_HouseholdId(household.getHouseholdId())
        .stream()
        .map(ShoppingItemResponseDto::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves a shopping item by its ID, ensuring the user has access.
   *
   * @param itemId the ID of the shopping item
   * @param dto    the request dto containing item details
   * @param email  the authenticated user's email
   * @throws EntityNotFoundException if the item is not found
   * @throws SecurityException if the user does not have access to the item
   */
  @Transactional
  public void updateItem(Long itemId, ShoppingItemRequestDto dto, String email) {
    ShoppingItem item = getAuthorizedItem(itemId, email);
    item.setName(dto.getName());
    item.setQuantity(dto.getQuantity());
    shoppingItemRepo.save(item);
    log.debug("Updated shopping item with ID: {}", itemId);
  }

  /**
   * Toggles the purchased status of a shopping item.
   *
   * @param itemId the ID of the shopping item
   * @param email  the authenticated user's email
   */
  @Transactional
  public void togglePurchased(Long itemId, String email) {
    ShoppingItem item = getAuthorizedItem(itemId, email);
    if (!item.isPurchased()) {
      item.setPurchased(true);
      item.setPurchasedBy(getAuthenticatedUser(email));
      item.setPurchasedAt(LocalDateTime.now());
    } else {
      item.setPurchased(false);
      item.setPurchasedBy(null);
      item.setPurchasedAt(null);
    }
  }

  /**
   * Deletes a shopping item by its ID, ensuring the user has access.
   *
   * @param itemId the ID of the shopping item
   * @param email  the authenticated user's email
   */
  public void deleteItem(Long itemId, String email) {
    ShoppingItem item = getAuthorizedItem(itemId, email);
    shoppingItemRepo.delete(item);
    log.debug("Deleted shopping item with ID: {}", itemId);
  }

  /**
   * Retrieves a shopping item by its ID, ensuring the user has access.
   *
   * @param itemId the ID of the shopping item
   * @param email  the authenticated user's email
   * @return the shopping item
   * @throws EntityNotFoundException if the item is not found
   * @throws AccessDeniedException if the user does not have access to the item
   */
  private ShoppingItem getAuthorizedItem(Long itemId, String email) {
    ShoppingItem item = shoppingItemRepo.findById(itemId)
        .orElseThrow(() -> new EntityNotFoundException("Item not found"));
    User user = userRepo.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
    if (!item.getHousehold().getHouseholdId().equals(user.getHousehold().getHouseholdId())) {
      throw new AccessDeniedException("Access denied: not in same household");
    }
    return item;
  }

  /**
   * Retrieves the authenticated user by their email.
   *
   * @param email the authenticated user's email
   * @return the authenticated user
   * @throws EntityNotFoundException if the user is not found
   */
  private User getAuthenticatedUser(String email) {
    return userRepo.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
  }
}
