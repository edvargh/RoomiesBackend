package com.roomies.service;

import com.roomies.dto.HouseholdCreateDto;
import com.roomies.dto.JoinHouseholdRequestDto;
import com.roomies.entity.Household;
import com.roomies.entity.Role;
import com.roomies.entity.User;
import com.roomies.repository.HouseholdRepository;
import com.roomies.repository.UserRepository;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling household-related operations.
 */
@Service
public class HouseholdService {

  private static final Logger log = LoggerFactory.getLogger(HouseholdService.class);
  private final HouseholdRepository householdRepo;
  private final UserRepository userRepo;

  public HouseholdService(HouseholdRepository householdRepo, UserRepository userRepo) {
    this.householdRepo = householdRepo;
    this.userRepo = userRepo;
  }

  /**
   * Creates a new household.
   *
   * @param dto the household creation data transfer object
   * @throws AccessDeniedException if the user has not confirmed their email
   */
  @Transactional
  public void createHousehold(HouseholdCreateDto dto, String email) {
    User user = userRepo.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

    if (!user.isConfirmed()) {
      throw new AccessDeniedException("User must confirm email before creating a household.");
    }

    Household household = new Household();
    household.setName(dto.getName());
    household.setJoinCode(generateJoinCode());
    log.debug("Creating household with name: {}", dto.getName());
    householdRepo.save(household);
    user.setHousehold(household);
    user.setRole(Role.ADMIN);
    log.debug("Household created with ID: {}", household.getHouseholdId());
  }

  @Transactional
  public void joinHousehold(JoinHouseholdRequestDto dto, String email) {
    User user = userRepo.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

    Household household = householdRepo.findByJoinCode(dto.getJoinCode())
        .orElseThrow(() -> new IllegalArgumentException("Household not found with join code: " + dto.getJoinCode()));

    if (user.getHousehold() != null) {
      throw new IllegalStateException("User already belongs to a household");
    }

    user.setHousehold(household);
    userRepo.save(user);
    log.debug("User {} joined household {}", user.getEmail(), household.getHouseholdId());
  }

  /**
   * Generates a unique, random join code for the household.
   *
   * @return a random join code
   */
  private String generateJoinCode() {
    int length = 6;
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    SecureRandom random = new SecureRandom();
    String code;

    do {
      StringBuilder sb = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
        sb.append(chars.charAt(random.nextInt(chars.length())));
      }
      code = sb.toString();
    } while (householdRepo.existsByJoinCode(code));

    return code;
  }
}
