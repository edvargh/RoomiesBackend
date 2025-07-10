package com.roomies.controller;

import com.roomies.dto.LoginRequestDto;
import com.roomies.dto.RegisterRequestDto;
import com.roomies.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for handling authentication requests.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Register a new user.
   *
   * @param req the registration request containing user details
   * @return a response indicating success
   */
  @PostMapping("/register")
  public ResponseEntity<Map<String,String>> register(
      @Valid @RequestBody RegisterRequestDto req) {
    authService.registerUser(req);
    return ResponseEntity.ok(Map.of("message",
        "Registration successful – check your e-mail"));
  }

  /**
   * Log in a user.
   *
   * @param req the login request containing user credentials
   * @return a JWT token if login is successful
   */
  @PostMapping("/login")
  public ResponseEntity<Map<String,String>> login(
      @Valid @RequestBody LoginRequestDto req) {
    String token = authService.loginUser(req);
    return ResponseEntity.ok(Map.of("token", token));
  }
}
