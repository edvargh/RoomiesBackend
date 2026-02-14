package com.roomies.service;

import com.roomies.dto.shoppingitem.ShoppingItemRequestDto;
import com.roomies.entity.Household;
import com.roomies.entity.ShoppingItem;
import com.roomies.entity.User;
import com.roomies.repository.ShoppingItemRepository;
import com.roomies.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;

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

  @Nested
  class MarkPurchasedBatch {

    private User makeUser(String email, Long hhId) {
      User u = new User();
      u.setEmail(email);
      Household h = new Household();
      h.setHouseholdId(hhId);
      u.setHousehold(h);
      return u;
    }

    private ShoppingItem makeItem(Long id, Long hhId, boolean purchased, String name) {
      ShoppingItem it = new ShoppingItem();
      it.setItemId(id);
      Household h = new Household();
      h.setHouseholdId(hhId);
      it.setHousehold(h);
      it.setPurchased(purchased);
      it.setName(name);
      return it;
    }

    @Test
    void shouldMarkItemsPurchased_whenAllInHouseholdAndNotPurchased() {
      String email = "user@example.com";
      Long hhId = 1L;
      User user = makeUser(email, hhId);

      ShoppingItem i1 = makeItem(10L, hhId, false, "Milk");
      ShoppingItem i2 = makeItem(11L, hhId, false, "Bread");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findByHousehold_HouseholdIdAndItemIdIn(hhId, Arrays.asList(10L, 11L)))
          .thenReturn(Arrays.asList(i1, i2));

      List<String> changed = shoppingItemService.markPurchasedBatch(Arrays.asList(10L, 11L), email);

      assertEquals(2, changed.size());
      assertTrue(i1.isPurchased());
      assertTrue(i2.isPurchased());
      assertEquals(user, i1.getPurchasedBy());
      assertEquals(user, i2.getPurchasedBy());
      assertNotNull(i1.getPurchasedAt());
      assertNotNull(i2.getPurchasedAt());

      verify(shoppingItemRepo).saveAll(argThat(list -> StreamSupport.stream(list.spliterator(), false)
          .collect(Collectors.toSet())
          .containsAll(Arrays.asList(i1, i2))));
    }

    @Test
    void shouldSkipAlreadyPurchasedItems_andOnlySaveWhenSomethingChanged() {
      String email = "user@example.com";
      Long hhId = 1L;
      User user = makeUser(email, hhId);

      ShoppingItem already = makeItem(10L, hhId, true, "Eggs");
      already.setPurchasedBy(user);
      already.setPurchasedAt(LocalDateTime.now().minusHours(1));

      ShoppingItem notYet = makeItem(11L, hhId, false, "Butter");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findByHousehold_HouseholdIdAndItemIdIn(hhId, Arrays.asList(10L, 11L)))
          .thenReturn(Arrays.asList(already, notYet));

      List<String> changed = shoppingItemService.markPurchasedBatch(Arrays.asList(10L, 11L), email);

      assertEquals(1, changed.size());
      assertEquals("Butter", changed.get(0));
      assertTrue(notYet.isPurchased());
      assertEquals(user, notYet.getPurchasedBy());
      assertNotNull(notYet.getPurchasedAt());

      verify(shoppingItemRepo).saveAll(anyList());
    }

    @Test
    void shouldNotSaveWhenAllAlreadyPurchased() {
      String email = "user@example.com";
      Long hhId = 1L;
      User user = makeUser(email, hhId);

      ShoppingItem a = makeItem(10L, hhId, true, "A");
      ShoppingItem b = makeItem(11L, hhId, true, "B");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findByHousehold_HouseholdIdAndItemIdIn(hhId, Arrays.asList(10L, 11L)))
          .thenReturn(Arrays.asList(a, b));

      List<String> changed = shoppingItemService.markPurchasedBatch(Arrays.asList(10L, 11L), email);

      assertTrue(changed.isEmpty());
      verify(shoppingItemRepo, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowWhenAnyItemNotInSameHousehold() {
      String email = "user@example.com";
      Long hhId = 1L;
      User user = makeUser(email, hhId);

      ShoppingItem i1 = makeItem(10L, hhId, false, "Milk");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(shoppingItemRepo.findByHousehold_HouseholdIdAndItemIdIn(hhId, Arrays.asList(10L, 99L)))
          .thenReturn(Collections.singletonList(i1));

      List<Long> ids = Arrays.asList(10L, 99L);
      assertThrows(AccessDeniedException.class,
          () -> shoppingItemService.markPurchasedBatch(ids, email));

      verify(shoppingItemRepo, never()).saveAll(anyList());
    }

    @Test
    void shouldThrowWhenNoIdsProvided() {
      String email = "user@example.com";
      List<Long> ids = Collections.emptyList();

      assertThrows(IllegalArgumentException.class,
          () -> shoppingItemService.markPurchasedBatch(ids, email));

      verify(shoppingItemRepo, never()).saveAll(anyList());
      verifyNoInteractions(userRepo);
    }

    @Test
    void shouldThrowWhenTooManyIds() {
      String email = "user@example.com";

      // build a list of size 201
      List<Long> ids = Arrays.asList(new Long[201]);
      for (int i = 0; i < ids.size(); i++) ids.set(i, (long) i + 1);

      assertThrows(IllegalArgumentException.class,
          () -> shoppingItemService.markPurchasedBatch(ids, email));

      verify(shoppingItemRepo, never()).saveAll(anyList());
      verifyNoInteractions(userRepo);
    }

    @Test
    void shouldThrowWhenUserHasNoHousehold() {
      String email = "user@example.com";
      User user = new User();
      user.setEmail(email);
      user.setHousehold(null);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));


      List<Long> ids = Arrays.asList(1L, 2L);

      assertThrows(IllegalStateException.class,
          () -> shoppingItemService.markPurchasedBatch(ids, email));

      verify(shoppingItemRepo, never()).saveAll(anyList());
    }
  }
}