package com.roomies.service;

import com.roomies.dto.UserResponseDto;
import com.roomies.dto.UserUpdateRequestDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepo;
  @Mock private PasswordEncoder passwordEncoder;

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

  @Nested
  class UpdateCurrentUser {

    @Test
    void shouldUpdateDisplayNameOnly() {
      // Arrange
      String email = "user@example.com";
      User user = new User();
      user.setUserId(1L);
      user.setEmail(email);
      user.setDisplayName("Old Name");
      user.setPassword("$2a$10$hash"); // existing hash

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(userRepo.save(user)).thenReturn(user);

      UserUpdateRequestDto dto = new UserUpdateRequestDto();
      dto.setDisplayName("New Name"); // password fields null

      // Act
      UserResponseDto resp = userService.updateCurrentUser(dto, email);

      // Assert
      assertEquals("New Name", user.getDisplayName());
      assertEquals("New Name", resp.getDisplayName());
      verify(userRepo).findByEmail(email);
      verify(userRepo).save(user);
      verifyNoMoreInteractions(userRepo);
      verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldChangePasswordWhenCurrentMatches() {
      // Arrange
      String email = "user@example.com";
      User user = new User();
      user.setUserId(1L);
      user.setEmail(email);
      user.setDisplayName("Name");
      user.setPassword("$2a$10$oldhash");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("oldPass", "$2a$10$oldhash")).thenReturn(true);
      when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$newhash");
      when(userRepo.save(user)).thenReturn(user);

      UserUpdateRequestDto dto = new UserUpdateRequestDto();
      dto.setCurrentPassword("oldPass");
      dto.setNewPassword("newPass123");

      // Act
      UserResponseDto resp = userService.updateCurrentUser(dto, email);

      // Assert
      assertEquals("$2a$10$newhash", user.getPassword());
      assertEquals("Name", resp.getDisplayName()); // unchanged
      verify(passwordEncoder).matches("oldPass", "$2a$10$oldhash");
      verify(passwordEncoder).encode("newPass123");
      verify(userRepo).save(user);
    }

    @Test
    void shouldThrowWhenChangingPasswordWithoutCurrentPassword() {
      // Arrange
      String email = "user@example.com";
      User user = new User();
      user.setEmail(email);
      user.setPassword("$2a$10$oldhash");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      UserUpdateRequestDto dto = new UserUpdateRequestDto();
      dto.setNewPassword("newPass123"); // currentPassword missing

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> userService.updateCurrentUser(dto, email));
      assertTrue(ex.getMessage().toLowerCase().contains("current password"));

      verify(userRepo).findByEmail(email);
      verify(userRepo, never()).save(any());
      verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowWhenCurrentPasswordIsIncorrect() {
      // Arrange
      String email = "user@example.com";
      User user = new User();
      user.setEmail(email);
      user.setPassword("$2a$10$oldhash");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("wrong", "$2a$10$oldhash")).thenReturn(false);

      UserUpdateRequestDto dto = new UserUpdateRequestDto();
      dto.setCurrentPassword("wrong");
      dto.setNewPassword("newPass123");

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> userService.updateCurrentUser(dto, email));
      assertTrue(ex.getMessage().toLowerCase().contains("incorrect"));

      verify(passwordEncoder).matches("wrong", "$2a$10$oldhash");
      verify(userRepo, never()).save(any());
    }

    @Test
    void shouldNoOpWhenNothingChanges() {
      // Arrange
      String email = "user@example.com";
      User user = new User();
      user.setEmail(email);
      user.setDisplayName("Same Name");
      user.setPassword("$2a$10$hash");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      UserUpdateRequestDto dto = new UserUpdateRequestDto();
      dto.setDisplayName("Same Name"); // same as current
      // no password fields

      // Act
      UserResponseDto resp = userService.updateCurrentUser(dto, email);

      // Assert
      assertEquals("Same Name", resp.getDisplayName());
      verify(userRepo).findByEmail(email);
      verify(userRepo, never()).save(any());
      verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldHandleNullFieldsGracefully() {
      // Arrange
      String email = "user@example.com";
      User user = new User();
      user.setEmail(email);
      user.setDisplayName("Name");
      user.setPassword("$2a$10$hash");

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      UserUpdateRequestDto dto = new UserUpdateRequestDto();
      dto.setDisplayName(null);
      dto.setCurrentPassword(null);
      dto.setNewPassword(null);

      // Act
      UserResponseDto resp = userService.updateCurrentUser(dto, email);

      // Assert
      assertEquals("Name", resp.getDisplayName());
      verify(userRepo).findByEmail(email);
      verify(userRepo, never()).save(any());
      verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowWhenUserNotFoundOnUpdate() {
      // Arrange
      String email = "missing@example.com";
      when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

      UserUpdateRequestDto dto = new UserUpdateRequestDto();
      dto.setDisplayName("X");

      // Act & Assert
      assertThrows(EntityNotFoundException.class, () -> userService.updateCurrentUser(dto, email));
      verify(userRepo).findByEmail(email);
      verify(userRepo, never()).save(any());
    }
  }

  @Nested
  class DeleteCurrentUser {

    @Test
    void shouldDeleteCurrentUser() {
      // Arrange
      String email = "user@example.com";
      User user = new User();
      user.setUserId(1L);
      user.setEmail(email);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      // Act
      assertDoesNotThrow(() -> userService.deleteCurrentUser(email));

      // Assert
      verify(userRepo).findByEmail(email);
      verify(userRepo).delete(user);
    }

    @Test
    void shouldThrowWhenUserNotFoundOnDelete() {
      // Arrange
      String email = "missing@example.com";
      when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(EntityNotFoundException.class, () -> userService.deleteCurrentUser(email));

      verify(userRepo).findByEmail(email);
      verify(userRepo, never()).delete(any());
    }
  }
}

