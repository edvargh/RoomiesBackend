package com.roomies.service;

import com.roomies.dto.UserResponseDto;
import com.roomies.entity.Household;
import com.roomies.entity.Role;
import com.roomies.entity.User;
import com.roomies.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepo;

  @InjectMocks private UserService userService;

  @Nested
  class GetCurrentUser {

    @Test
    void shouldReturnDtoWithHousehold() {
      // Arrange
      String email = "alice@example.com";

      Household h = new Household();
      h.setHouseholdId(7L);
      h.setName("Solsiden 3B");

      User user = new User();
      user.setUserId(42L);
      user.setEmail(email);
      user.setDisplayName("Alice");
      user.setHousehold(h);
      user.setRole(Role.MEMBER);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      // Act
      UserResponseDto dto = userService.getCurrentUser(email);

      // Assert
      assertNotNull(dto);
      assertEquals(42L, dto.getUserId());
      assertEquals(email, dto.getEmail());
      assertEquals("Alice", dto.getDisplayName());
      assertEquals(7L, dto.getHouseholdId());
      assertEquals("Solsiden 3B", dto.getHouseholdName());
      assertEquals(Role.MEMBER, dto.getRole());

      verify(userRepo).findByEmail(email);
      verifyNoMoreInteractions(userRepo);
    }

    @Test
    void shouldReturnDtoWithoutHouseholdWhenNull() {
      // Arrange
      String email = "bob@example.com";

      User user = new User();
      user.setUserId(99L);
      user.setEmail(email);
      user.setDisplayName("Bob");
      user.setHousehold(null); // no household
      user.setRole(Role.ADMIN);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      // Act
      UserResponseDto dto = userService.getCurrentUser(email);

      // Assert
      assertNotNull(dto);
      assertEquals(99L, dto.getUserId());
      assertEquals(email, dto.getEmail());
      assertEquals("Bob", dto.getDisplayName());
      assertNull(dto.getHouseholdId());
      assertNull(dto.getHouseholdName());
      assertEquals(Role.ADMIN, dto.getRole());

      verify(userRepo).findByEmail(email);
      verifyNoMoreInteractions(userRepo);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
      // Arrange
      String email = "missing@example.com";
      when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(EntityNotFoundException.class, () -> userService.getCurrentUser(email));

      verify(userRepo).findByEmail(email);
      verifyNoMoreInteractions(userRepo);
    }
  }
}

