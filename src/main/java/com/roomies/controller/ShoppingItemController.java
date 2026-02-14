package com.roomies.controller;

import com.roomies.dto.shoppingitem.ShoppingItemIdListRequestDto;
import com.roomies.dto.shoppingitem.ShoppingItemRequestDto;
import com.roomies.dto.shoppingitem.ShoppingItemResponseDto;
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
   * Marks multiple shopping items as purchased.
   *
   * @param request     the request DTO containing the list of item IDs to mark as purchased
   * @param userDetails the authenticated user's details
   * @return a response entity with a success message and details of updated items
   */
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/purchased")
  public ResponseEntity<Map<String, Object>> markPurchasedBatch(
      @Valid @RequestBody ShoppingItemIdListRequestDto request,
      @AuthenticationPrincipal UserDetails userDetails) {

    List<String> changed = shoppingItemService.markPurchasedBatch(request.getIds(), userDetails.getUsername());
    return ResponseEntity.ok(Map.of(
        MESSAGE_KEY, "Items marked purchased",
        "updatedCount", changed.size(),
        "items", changed
    ));
  }

  /**
   * Deletes a shopping item.
   *
   * @param id          the ID of the shopping item to delete
   * @param userDetails the authenticated user's details
   * @return a response entity with a success message
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteItem(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    shoppingItemService.deleteItem(id, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Shopping item deleted successfully"));
  }
}
