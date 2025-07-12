package com.roomies.repository;

import com.roomies.entity.Household;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for handling collective related requests.
 */
public interface HouseholdRepository extends JpaRepository<Household, Long> {

  /**
   * Finds a household by its join code.
   *
   * @param joinCode the join code of the household
   * @return an Optional containing the Household if found, or empty if not found
   */
  Optional<Household> findByJoinCode(String joinCode);

  /**
   * Checks if a household exists with the given join code.
   *
   * @param joinCode the join code to check
   * @return true if a household exists with the given join code, false otherwise
   */
  boolean existsByJoinCode(String joinCode);

}
