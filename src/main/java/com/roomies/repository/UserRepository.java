package com.roomies.repository;

import com.roomies.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for handling user related requests.
 */
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  List<User> findByHousehold_HouseholdIdOrderByDisplayNameAsc(Long householdId);
  Optional<User> findByConfirmationToken(String token);
}
