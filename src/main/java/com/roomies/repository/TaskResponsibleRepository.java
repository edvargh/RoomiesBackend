package com.roomies.repository;

import com.roomies.entity.TaskResponsible;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/**
 * Repository for handling task responsible related requests.
 */
public interface TaskResponsibleRepository extends JpaRepository<TaskResponsible, Long>{
  boolean existsByTask_TaskIdAndUser_UserId(Long taskId, Long userId);
  TaskResponsible findByTask_TaskIdAndPosition(Long taskId, int position);


  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<TaskResponsible> findAllByTask_TaskIdOrderByPositionAsc(Long taskId);

  List<TaskResponsible> findAllByTask_TaskIdInOrderByTask_TaskIdAscPositionAsc(List<Long> taskIds);
}
