package com.roomies.service;

import com.roomies.dto.HouseholdCreateDto;
import com.roomies.dto.HouseholdDetailsResponseDto;
import com.roomies.dto.JoinHouseholdRequestDto;
import com.roomies.entity.Household;
import com.roomies.entity.Role;
import com.roomies.entity.User;
import com.roomies.repository.HouseholdRepository;
import com.roomies.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.util.List;
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
    User user = getUser(email);

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

  /**
   * Allows a user to join an existing household using a join code.
   *
   * @param dto the request data transfer object containing the join code
   * @param email the email of the user trying to join
   * @throws IllegalArgumentException if the user or household is not found
   * @throws IllegalStateException if the user already belongs to a household
   */
  @Transactional
  public void joinHousehold(JoinHouseholdRequestDto dto, String email) {
    User user = getUser(email);

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
   * Retrieves the details of the authenticated user's household.
   *
   * @param email the email of the authenticated user
   * @return a DTO containing household details and members
   * @throws IllegalArgumentException if the user is not found
   * @throws IllegalStateException if the user does not belong to a household
   */
  @Transactional(readOnly = true)
  public HouseholdDetailsResponseDto getMyHouseholdDetails(String email) {
    User user = getUser(email);
    Household household = user.getHousehold();
    if (household == null) {
      throw new IllegalStateException("User must be part of a household");
    }
    List<User> members = userRepo.findByHousehold_HouseholdIdOrderByDisplayNameAsc(household.getHouseholdId());
    log.debug("Returning household {} with {} members", household.getHouseholdId(), members.size());
    return HouseholdDetailsResponseDto.fromEntity(household, members);
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

  private User getUser(String email) {
    return userRepo.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
  }
}
