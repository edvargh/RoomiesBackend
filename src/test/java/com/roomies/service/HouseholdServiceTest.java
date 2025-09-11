package com.roomies.service;

import com.roomies.dto.household.HouseholdCreateDto;
import com.roomies.dto.household.HouseholdDetailsResponseDto;
import com.roomies.dto.household.JoinHouseholdRequestDto;
import com.roomies.entity.Household;
import com.roomies.entity.Role;
import com.roomies.entity.User;
import com.roomies.repository.HouseholdRepository;
import com.roomies.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HouseholdService.
 */
@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

  @Mock private HouseholdRepository householdRepo;
  @Mock private UserRepository userRepo;

  @InjectMocks private HouseholdService householdService;

  @Nested
  class CreateHousehold {

    @Test
    void shouldCreateHouseholdWhenUserIsConfirmed() {
      // Arrange
      String email = "user@example.com";
      HouseholdCreateDto dto = new HouseholdCreateDto();
      dto.setName("Cool House");

      User user = new User();
      user.setEmail(email);
      user.setConfirmed(true);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(householdRepo.existsByJoinCode(anyString())).thenReturn(false);

      // Act
      householdService.createHousehold(dto, email);

      // Assert
      assertEquals("Cool House", user.getHousehold().getName());
      assertEquals(Role.ADMIN, user.getRole());
      verify(householdRepo).save(any(Household.class));
    }

    @Test
    void shouldThrowIfUserIsNotConfirmed() {
      // Arrange
      String email = "unconfirmed@example.com";
      HouseholdCreateDto dto = new HouseholdCreateDto();
      dto.setName("Unconfirmed House");

      User user = new User();
      user.setEmail(email);
      user.setConfirmed(false);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      // Act & Assert
      assertThrows(AccessDeniedException.class,
          () -> householdService.createHousehold(dto, email));

      verify(householdRepo, never()).save(any());
    }
  }

  @Nested
  class JoinHousehold {

    @Test
    void shouldJoinHouseholdWhenUserIsNotAlreadyMember() {
      // Arrange
      String email = "joiner@example.com";
      String joinCode = "ABC123";
      JoinHouseholdRequestDto dto = new JoinHouseholdRequestDto();
      dto.setJoinCode(joinCode);

      User user = new User();
      user.setEmail(email);
      user.setHousehold(null);

      Household household = new Household();
      household.setJoinCode(joinCode);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(householdRepo.findByJoinCode(joinCode)).thenReturn(Optional.of(household));

      // Act
      householdService.joinHousehold(dto, email);

      // Assert
      assertEquals(household, user.getHousehold());
      verify(userRepo).save(user);
    }

    @Test
    void shouldThrowIfUserAlreadyInHousehold() {
      // Arrange
      String email = "member@example.com";
      String joinCode = "DEF456";
      JoinHouseholdRequestDto dto = new JoinHouseholdRequestDto();
      dto.setJoinCode(joinCode);

      User user = new User();
      user.setEmail(email);
      user.setHousehold(new Household()); // already a member

      Household dummyHousehold = new Household();

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(householdRepo.findByJoinCode(joinCode)).thenReturn(Optional.of(dummyHousehold));

      // Act & Assert
      assertThrows(IllegalStateException.class,
          () -> householdService.joinHousehold(dto, email));

      verify(userRepo, never()).save(any());
    }

    @Test
    void shouldThrowIfJoinCodeNotFound() {
      // Arrange
      String email = "missing@example.com";
      String joinCode = "XXXXXX";
      JoinHouseholdRequestDto dto = new JoinHouseholdRequestDto();
      dto.setJoinCode(joinCode);

      User user = new User();
      user.setEmail(email);
      user.setHousehold(null);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(householdRepo.findByJoinCode(joinCode)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(IllegalArgumentException.class,
          () -> householdService.joinHousehold(dto, email));
    }
  }

  @Nested
  class GetMyHouseholdDetails {

    @Test
    void shouldReturnHouseholdDetailsForMember() {
      // Arrange
      String email = "member@example.com";

      Household household = new Household();
      household.setHouseholdId(42L);
      household.setName("Cool House");
      household.setJoinCode("ABC123");

      User currentUser = new User();
      currentUser.setEmail(email);
      currentUser.setHousehold(household);

      User alice = new User();
      alice.setUserId(1L);
      alice.setEmail("alice@example.com");
      alice.setDisplayName("Alice");
      alice.setHousehold(household);

      User bob = new User();
      bob.setUserId(2L);
      bob.setEmail("bob@example.com");
      bob.setDisplayName("Bob");
      bob.setHousehold(household);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(currentUser));
      when(userRepo.findByHousehold_HouseholdIdOrderByDisplayNameAsc(42L))
          .thenReturn(List.of(alice, bob));

      // Act
      HouseholdDetailsResponseDto dto = householdService.getMyHouseholdDetails(email);

      // Assert
      assertNotNull(dto);
      assertEquals(42L, dto.getHouseholdId());
      assertEquals("Cool House", dto.getName());
      assertEquals("ABC123", dto.getJoinCode());
      assertNotNull(dto.getMembers());
      assertEquals(2, dto.getMembers().size());
      // order should match the sorted list from repo
      assertEquals("Alice", dto.getMembers().get(0).getDisplayName());
      assertEquals("alice@example.com", dto.getMembers().get(0).getEmail());
      assertEquals(Long.valueOf(1L), dto.getMembers().get(0).getUserId());

      assertEquals("Bob", dto.getMembers().get(1).getDisplayName());
      assertEquals("bob@example.com", dto.getMembers().get(1).getEmail());
      assertEquals(Long.valueOf(2L), dto.getMembers().get(1).getUserId());

      verify(userRepo).findByEmail(email);
      verify(userRepo).findByHousehold_HouseholdIdOrderByDisplayNameAsc(42L);
      verifyNoMoreInteractions(userRepo);
    }

    @Test
    void shouldThrowIfUserNotFound() {
      // Arrange
      String email = "missing@example.com";
      when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(EntityNotFoundException.class,
          () -> householdService.getMyHouseholdDetails(email));

      verify(userRepo).findByEmail(email);
      verify(userRepo, never()).findByHousehold_HouseholdIdOrderByDisplayNameAsc(anyLong());
    }

    @Test
    void shouldThrowIfUserHasNoHousehold() {
      // Arrange
      String email = "loner@example.com";
      User user = new User();
      user.setEmail(email);
      user.setHousehold(null);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      // Act & Assert
      assertThrows(IllegalStateException.class,
          () -> householdService.getMyHouseholdDetails(email));

      verify(userRepo).findByEmail(email);
      verify(userRepo, never()).findByHousehold_HouseholdIdOrderByDisplayNameAsc(anyLong());
    }
  }
}
