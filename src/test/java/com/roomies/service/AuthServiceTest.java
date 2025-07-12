package com.roomies.service;

import com.roomies.dto.LoginRequestDto;
import com.roomies.dto.LoginResponseDto;
import com.roomies.dto.RefreshTokenRequestDto;
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
      when(jwtService.generateToken(request.getEmail())).thenReturn("access-token");
      when(jwtService.generateRefreshToken(request.getEmail())).thenReturn("refresh-token");

      // Act
      LoginResponseDto response = authService.loginUser(request);

      // Assert
      assertEquals("access-token", response.getAccessToken());
      assertEquals("refresh-token", response.getRefreshToken());
      verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepo).save(user);
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

  @Nested
  class RefreshAccessToken {

    @Test
    void shouldRefreshAccessAndReturnNewTokens() {
      // Arrange
      String oldRefreshToken = "valid-refresh-token";
      String email = "user@example.com";

      RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
      dto.setRefreshToken(oldRefreshToken);

      User user = new User();
      user.setEmail(email);
      user.setRefreshToken(oldRefreshToken);

      when(jwtService.extractUsernameFromRefreshToken(oldRefreshToken)).thenReturn(email);
      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(jwtService.isRefreshTokenValid(oldRefreshToken, email)).thenReturn(true);
      when(jwtService.generateToken(email)).thenReturn("new-access-token");
      when(jwtService.generateRefreshToken(email)).thenReturn("new-refresh-token");

      // Act
      LoginResponseDto response = authService.refreshAccessToken(dto);

      // Assert
      assertEquals("new-access-token", response.getAccessToken());
      assertEquals("new-refresh-token", response.getRefreshToken());
      verify(userRepo).save(user);
    }

    @Test
    void shouldThrowIfRefreshTokenDoesNotMatchStoredToken() {
      // Arrange
      String providedToken = "invalid-token";
      String email = "user@example.com";

      RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
      dto.setRefreshToken(providedToken);

      User user = new User();
      user.setEmail(email);
      user.setRefreshToken("stored-token");

      when(jwtService.extractUsernameFromRefreshToken(providedToken)).thenReturn(email);
      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authService.refreshAccessToken(dto));
      assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    void shouldThrowIfRefreshTokenIsInvalidOrExpired() {
      // Arrange
      String refreshToken = "expired-token";
      String email = "user@example.com";

      RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
      dto.setRefreshToken(refreshToken);

      User user = new User();
      user.setEmail(email);
      user.setRefreshToken(refreshToken);

      when(jwtService.extractUsernameFromRefreshToken(refreshToken)).thenReturn(email);
      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(jwtService.isRefreshTokenValid(refreshToken, email)).thenReturn(false);

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authService.refreshAccessToken(dto));
      assertEquals("Expired or invalid refresh token", ex.getMessage());
    }

    @Test
    void shouldThrowIfUserNotFound() {
      // Arrange
      String refreshToken = "token";
      String email = "ghost@example.com";

      RefreshTokenRequestDto dto = new RefreshTokenRequestDto();
      dto.setRefreshToken(refreshToken);

      when(jwtService.extractUsernameFromRefreshToken(refreshToken)).thenReturn(email);
      when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

      // Act & Assert
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> authService.refreshAccessToken(dto));
      assertEquals("User not found: " + email, ex.getMessage());
    }
  }

}
