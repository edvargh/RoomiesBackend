package com.roomies.controller;

import com.roomies.dto.user.UserResponseDto;
import com.roomies.dto.user.UserUpdateRequestDto;
import com.roomies.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Returns the authenticated user's own profile.
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ResponseEntity<UserResponseDto> getMe(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
  }

  /** Update current user's displayName and/or password (requires currentPassword for password changes). */
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/me")
  public ResponseEntity<UserResponseDto> updateMe(
      @Valid @RequestBody UserUpdateRequestDto dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userService.updateCurrentUser(dto, userDetails.getUsername()));
  }

  /** Delete current user's account. */
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/me")
  public ResponseEntity<Map<String, String>> deleteMe(@AuthenticationPrincipal UserDetails userDetails) {
    userService.deleteCurrentUser(userDetails.getUsername());
    return ResponseEntity.ok(Map.of("message", "Account deleted"));
  }
}
