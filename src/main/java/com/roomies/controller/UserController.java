package com.roomies.controller;

import com.roomies.dto.UserResponseDto;
import com.roomies.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private static final String MESSAGE_KEY = "message";

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

}
