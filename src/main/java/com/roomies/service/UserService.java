package com.roomies.service;

import com.roomies.dto.UserResponseDto;
import com.roomies.entity.User;
import com.roomies.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for handling user self-service operations.
 */
@Service
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepo;

  public UserService(UserRepository userRepo) {
    this.userRepo = userRepo;
  }

  /**
   * Returns the authenticated user's own profile.
   */
  public UserResponseDto getCurrentUser(String email) {
    User user = getAuthenticatedUser(email);
    log.debug("Fetching current user profile for email: {}", email);
    return UserResponseDto.fromEntity(user);
  }

  /**
   * Retrieves the authenticated user by email.
   */
  private User getAuthenticatedUser(String email) {
    return userRepo.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
  }
}
