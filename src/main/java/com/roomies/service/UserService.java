package com.roomies.service;

import com.roomies.dto.user.UserResponseDto;
import com.roomies.dto.user.UserUpdateRequestDto;
import com.roomies.entity.User;
import com.roomies.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user self-service operations.
 */
@Service
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepo;

  public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Returns the authenticated user's own profile.
   */
  public UserResponseDto getCurrentUser(String email) {
    User user = getAuthenticatedUser(email);
    log.debug("Fetching current user profile for email: {}", email);
    return UserResponseDto.fromEntity(user);
  }

  /** Update current user's displayName and/or password. */
  @Transactional
  public UserResponseDto updateCurrentUser(UserUpdateRequestDto dto, String email) {
    User user = getAuthenticatedUser(email);
    boolean changed = false;

    // display name
    if (dto.getDisplayName() != null && !dto.getDisplayName().isBlank()
        && !dto.getDisplayName().equals(user.getDisplayName())) {
      user.setDisplayName(dto.getDisplayName());
      changed = true;
    }

    // password
    if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
      if (dto.getCurrentPassword() == null || dto.getCurrentPassword().isBlank()) {
        throw new IllegalArgumentException("Current password is required to change password");
      }
      if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
        throw new IllegalArgumentException("Current password is incorrect");
      }
      user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
      changed = true;
    }

    if (changed) {
      userRepo.save(user); // optional with managed entity, explicit for clarity
      log.debug("Updated profile for userId: {}", user.getUserId());
    } else {
      log.debug("No-op update for userId: {}", user.getUserId());
    }

    return UserResponseDto.fromEntity(user);
  }

  /** Delete the current user's account. */
  @Transactional
  public void deleteCurrentUser(String email) {
    User user = getAuthenticatedUser(email);

    userRepo.delete(user);
    log.debug("Deleted account for userId: {}", user.getUserId());
  }


  /**
   * Retrieves the authenticated user by email.
   */
  private User getAuthenticatedUser(String email) {
    return userRepo.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
  }
}
