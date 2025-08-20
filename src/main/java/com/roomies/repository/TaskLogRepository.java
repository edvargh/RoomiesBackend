package com.roomies.repository;

import com.roomies.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for handling task log related requests.
 */
public interface TaskLogRepository extends JpaRepository<TaskLog, Long>{
}
