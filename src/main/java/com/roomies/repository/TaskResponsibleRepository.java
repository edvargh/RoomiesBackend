package com.roomies.repository;

import com.roomies.entity.TaskResponsible;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for handling task responsible related requests.
 */
public interface TaskResponsibleRepository extends JpaRepository<TaskResponsible, Long>{
  boolean existsByTask_TaskIdAndUser_UserId(Long taskId, Long userId);
  TaskResponsible findByTask_TaskIdAndPosition(Long taskId, int position);
  List<TaskResponsible> findAllByTask_TaskIdOrderByPositionAsc(Long taskId);
}
