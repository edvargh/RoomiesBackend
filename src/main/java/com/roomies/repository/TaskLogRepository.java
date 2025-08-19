package com.roomies.repository;

import com.roomies.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long>{
}
