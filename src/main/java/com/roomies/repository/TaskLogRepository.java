package com.roomies.repository;

import com.roomies.entity.TaskLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for handling task log related requests.
 */
public interface TaskLogRepository extends JpaRepository<TaskLog, Long>{

  @EntityGraph(attributePaths = {"completedBy", "task"})
  Page<TaskLog> findByTask_Household_HouseholdId(Long householdId, Pageable pageable);
}
