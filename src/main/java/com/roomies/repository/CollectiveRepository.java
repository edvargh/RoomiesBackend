package com.roomies.repository;

import com.roomies.entity.Collective;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for handling collective related requests.
 */
public interface CollectiveRepository extends JpaRepository<Collective, Long> {
}
