package com.roomies.service;

import com.roomies.dto.ShoppingItemRequestDto;
import com.roomies.entity.Household;
import com.roomies.entity.ShoppingItem;
import com.roomies.entity.User;
import com.roomies.repository.ShoppingItemRepository;
import com.roomies.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShoppingItemService.
 */
@ExtendWith(MockitoExtension.class)
class ShoppingItemServiceTest {

  @Mock private ShoppingItemRepository shoppingItemRepo;
  @Mock private UserRepository userRepo;

  @InjectMocks private ShoppingItemService shoppingItemService;

  @Nested
  class CreateItem {
    @Test
    void shouldCreateShoppingItemWhenUserIsInHousehold() {
      String email = "user@example.com";
      ShoppingItemRequestDto dto = new ShoppingItemRequestDto();
      dto.setName("Milk");
      dto.setQuantity("2");

      User user = new User();
      user.setEmail(email);
      Household household = new Household();
      user.setHousehold(household);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.save(any(ShoppingItem.class))).thenAnswer(inv -> inv.getArgument(0));

      assertDoesNotThrow(() -> shoppingItemService.createItem(dto, email));

      verify(shoppingItemRepo).save(any(ShoppingItem.class));
    }

    @Test
    void shouldThrowWhenUserNotInHousehold() {
      String email = "user@example.com";
      ShoppingItemRequestDto dto = new ShoppingItemRequestDto();
      dto.setName("Milk");

      User user = new User();
      user.setEmail(email);
      user.setHousehold(null);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      assertThrows(IllegalStateException.class, () -> shoppingItemService.createItem(dto, email));
    }
  }

  @Nested
  class GetItemsForHousehold {
    @Test
    void shouldReturnItemsWhenUserInHousehold() {
      String email = "user@example.com";
      String name = "Test User";
      Household household = new Household();
      household.setHouseholdId(1L);

      User user = new User();
      user.setEmail(email);
      user.setDisplayName(name);
      user.setHousehold(household);

      ShoppingItem item = new ShoppingItem();
      item.setHousehold(household);
      item.setAddedBy(user);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findByHousehold_HouseholdId(1L)).thenReturn(List.of(item));

      assertEquals(1, shoppingItemService.getItemsForHousehold(email).size());
    }
  }

  @Nested
  class DeleteItem {
    @Test
    void shouldDeleteItemWhenUserInSameHousehold() {
      String email = "user@example.com";
      Household household = new Household();
      household.setHouseholdId(1L);

      User user = new User();
      user.setEmail(email);
      user.setHousehold(household);

      ShoppingItem item = new ShoppingItem();
      item.setItemId(10L);
      item.setHousehold(household);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findById(10L)).thenReturn(Optional.of(item));

      assertDoesNotThrow(() -> shoppingItemService.deleteItem(10L, email));

      verify(shoppingItemRepo).delete(item);
    }

    @Test
    void shouldThrowAccessDeniedIfUserNotInSameHousehold() {
      String email = "user@example.com";
      Household h1 = new Household(); h1.setHouseholdId(1L);
      Household h2 = new Household(); h2.setHouseholdId(2L);

      User user = new User();
      user.setEmail(email);
      user.setHousehold(h1);

      ShoppingItem item = new ShoppingItem();
      item.setItemId(10L);
      item.setHousehold(h2);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findById(10L)).thenReturn(Optional.of(item));

      assertThrows(org.springframework.security.access.AccessDeniedException.class,
          () -> shoppingItemService.deleteItem(10L, email));

      verify(shoppingItemRepo, never()).delete(any());
    }
  }

  @Nested
  class UpdateItem {
    @Test
    void shouldUpdateItemWhenUserIsAuthorized() {
      String email = "user@example.com";
      ShoppingItemRequestDto dto = new ShoppingItemRequestDto();
      dto.setName("Bread");
      dto.setQuantity("1");

      Household household = new Household();
      household.setHouseholdId(1L);

      User user = new User();
      user.setEmail(email);
      user.setHousehold(household);

      ShoppingItem item = new ShoppingItem();
      item.setItemId(20L);
      item.setHousehold(household);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findById(20L)).thenReturn(Optional.of(item));

      assertDoesNotThrow(() -> shoppingItemService.updateItem(20L, dto, email));

      verify(shoppingItemRepo).save(item);
      assertEquals("Bread", item.getName());
      assertEquals("1", item.getQuantity());
    }
  }
}