package com.roomies.repository;

import com.roomies.entity.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
  Optional<Task> findByTaskIdAndHousehold_HouseholdId(Long taskId, Long householdId);
  List<Task> findByHousehold_HouseholdId(Long householdId);
  List<Task> findByHousehold_HouseholdIdOrderByNextDueAsc(Long householdId);

}
