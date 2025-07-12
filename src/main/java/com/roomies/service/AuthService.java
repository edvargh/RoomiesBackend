package com.roomies.service;

import com.roomies.dto.LoginRequestDto;
import com.roomies.dto.RegisterRequestDto;
import com.roomies.entity.User;
import com.roomies.repository.UserRepository;
import com.roomies.security.JwtService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling authentication-related operations.
 */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final EmailService emailService;
  private final AuthenticationManager authManager;

  @Autowired
  public AuthService(UserRepository userRepo,
      PasswordEncoder passwordEncoder,
      JwtService jwtService, EmailService emailService,
      AuthenticationManager authManager) {
    this.userRepo        = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.jwtService      = jwtService;
    this.emailService    = emailService;
    this.authManager     = authManager;
  }

  /**
   * Registers a new user.
   * @param req the registration request containing user details
   */
  @Transactional
  public void registerUser(RegisterRequestDto req) {
    log.debug("Registering user {}", req.getEmail());

    if (userRepo.existsByEmail(req.getEmail())) {
      log.warn("Email already in use: {}", req.getEmail());
      throw new IllegalArgumentException("Email already in use");
    }

    User u = new User();
    u.setDisplayName(req.getDisplayName());
    u.setEmail(req.getEmail());
    u.setPassword(passwordEncoder.encode(req.getPassword()));

    String token = UUID.randomUUID().toString();

    u.setConfirmationToken(token);
    u.setConfirmed(false);

    userRepo.save(u);
    emailService.sendVerificationMail(req.getEmail(), token);
  }

  /**
   * Logs in a user and generates a JWT token.
   * @param req the login request containing email and password
   * @return the generated JWT token
   */
  public String loginUser(LoginRequestDto req) {
    log.debug("Authenticating {}", req.getEmail());

    User user = userRepo.findByEmail(req.getEmail())
        .orElseThrow(() -> {
          log.warn("User not found: {}", req.getEmail());
          return new IllegalArgumentException("Invalid email or password");
        });

    if (!user.isConfirmed()) {
      log.warn("User not confirmed: {}", req.getEmail());
      throw new IllegalArgumentException("User not confirmed");
    }

    authManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

    return jwtService.generateToken(req.getEmail());
  }
}
