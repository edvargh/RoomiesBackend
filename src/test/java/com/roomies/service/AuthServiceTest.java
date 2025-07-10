package com.roomies.service;

import com.roomies.dto.LoginRequestDto;
import com.roomies.dto.RegisterRequestDto;
import com.roomies.entity.Role;
import com.roomies.entity.User;
import com.roomies.repository.UserRepository;
import com.roomies.security.JwtService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepo;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private EmailService emailService;
  @Mock private AuthenticationManager authManager;

  @InjectMocks private AuthService authService;

  @Nested
  class RegisterUser {

    @Test
    void shouldRegisterUserSuccessfully() {
      // Arrange
      RegisterRequestDto request = new RegisterRequestDto();
      request.setEmail("test@example.com");
      request.setPassword("password123");
      request.setDisplayName("Test User");

      when(userRepo.existsByEmail(request.getEmail())).thenReturn(false);
      when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

      // Act
      authService.registerUser(request);

      // Assert
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepo).save(userCaptor.capture());
      verify(emailService).sendVerificationMail(eq("test@example.com"), anyString());

      User savedUser = userCaptor.getValue();
      assertEquals("Test User", savedUser.getDisplayName());
      assertEquals("test@example.com", savedUser.getEmail());
      assertEquals("encodedPassword", savedUser.getPassword());
      assertEquals(Role.MEMBER, savedUser.getRole());
      assertFalse(savedUser.isConfirmed());
      assertNotNull(savedUser.getConfirmationToken());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
      // Arrange
      RegisterRequestDto request = new RegisterRequestDto();
      request.setEmail("duplicate@example.com");

      when(userRepo.existsByEmail(request.getEmail())).thenReturn(true);

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authService.registerUser(request));
      assertEquals("Email already in use", ex.getMessage());
      verify(userRepo, never()).save(any());
      verify(emailService, never()).sendVerificationMail(any(), any());
    }
  }

  @Nested
  class LoginUser {

    @Test
    void shouldLoginConfirmedUser() {
      // Arrange
      LoginRequestDto request = new LoginRequestDto();
      request.setEmail("user@example.com");
      request.setPassword("password");

      User user = new User();
      user.setEmail("user@example.com");
      user.setPassword("encodedPassword");
      user.setConfirmed(true);

      when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
      when(jwtService.generateToken(request.getEmail())).thenReturn("jwt-token");

      // Act
      String token = authService.loginUser(request);

      // Assert
      assertEquals("jwt-token", token);
      verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
      // Arrange
      LoginRequestDto request = new LoginRequestDto();
      request.setEmail("notfound@example.com");

      when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.empty());

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authService.loginUser(request));
      assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUserIsNotConfirmed() {
      // Arrange
      LoginRequestDto request = new LoginRequestDto();
      request.setEmail("unconfirmed@example.com");

      User user = new User();
      user.setEmail("unconfirmed@example.com");
      user.setConfirmed(false);

      when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authService.loginUser(request));
      assertEquals("User not confirmed", ex.getMessage());
    }
  }
}
