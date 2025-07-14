package com.roomies.controller;

import com.roomies.dto.ShoppingItemRequestDto;
import com.roomies.dto.ShoppingItemResponseDto;
import com.roomies.service.ShoppingItemService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-items")
public class ShoppingItemController {

  private final ShoppingItemService shoppingItemService;
  private static final String MESSAGE_KEY = "message";

  public ShoppingItemController(ShoppingItemService shoppingItemService) {
    this.shoppingItemService = shoppingItemService;
  }

  /**
   * Creates a new shopping item.
   *
   * @param dto         the request DTO containing item details
   * @param userDetails the authenticated user's details
   * @return a response entity with a success message
   */
  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<Map<String, String>> createItem(
      @Valid @RequestBody ShoppingItemRequestDto dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    shoppingItemService.createItem(dto, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY,"Shopping item created successfully"));
  }

  /**
   * Retrieves all shopping items for the authenticated user's household.
   *
   * @param userDetails the authenticated user's details
   * @return a response entity containing the list of shopping items
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping
  public ResponseEntity<List<ShoppingItemResponseDto>> getItems(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(shoppingItemService.getItemsForHousehold(userDetails.getUsername()));
  }

  /**
   * Updates an existing shopping item.
   *
   * @param id          the ID of the shopping item to update
   * @param dto         the request DTO containing updated item details
   * @param userDetails the authenticated user's details
   * @return a response entity with a success message
   */
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{id}")
  public ResponseEntity<Map<String, String>> updateItem(
      @PathVariable Long id,
      @Valid @RequestBody ShoppingItemRequestDto dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    shoppingItemService.updateItem(id, dto, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Shopping item updated successfully"));
  }

  /**
   * Toggles the purchased status of a shopping item.
   *
   * @param id          the ID of the shopping item
   * @param userDetails the authenticated user's details
   * @return a response entity with a success message
   */
  @PutMapping("/{id}/purchased")
  public ResponseEntity<Map<String, String>> togglePurchased(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    shoppingItemService.togglePurchased(id, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Shopping item purchase status toggled successfully"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteItem(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    shoppingItemService.deleteItem(id, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Shopping item deleted successfully"));
  }
}
