package com.roomies.dto.task;

import com.roomies.entity.Task;
import com.roomies.entity.TaskLog;
import com.roomies.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public class TaskLogResponseDto {
  private Long logId;
  private Long taskId;
  private String taskDescription;
  private Long completedByUserId;
  private String completedByFullName;
  private LocalDateTime completedAt;

  public TaskLogResponseDto() {}

  public static TaskLogResponseDto fromEntity(TaskLog log) {
    TaskLogResponseDto dto = new TaskLogResponseDto();
    dto.setLogId(log.getLogId());
    dto.setCompletedAt(log.getCompletedAt());

    Task t = log.getTask();
    if (t != null) {
      dto.setTaskId(t.getTaskId());
      dto.setTaskDescription(t.getDescription());
    }

    User u = log.getCompletedBy();
    if (u != null) {
      dto.setCompletedByUserId(u.getUserId());
      dto.setCompletedByFullName(u.getDisplayName());
    }
    return dto;
  }

  public static List<TaskLogResponseDto> fromEntities(List<TaskLog> logs) {
    return logs.stream().map(TaskLogResponseDto::fromEntity).toList();
  }

  public Long getLogId() { return logId; }
  public void setLogId(Long logId) { this.logId = logId; }

  public Long getTaskId() { return taskId; }
  public void setTaskId(Long taskId) { this.taskId = taskId; }
  public String getTaskDescription() { return taskDescription; }
  public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

  public Long getCompletedByUserId() { return completedByUserId; }
  public void setCompletedByUserId(Long completedByUserId) { this.completedByUserId = completedByUserId; }

  public String getCompletedByFullName() { return completedByFullName; }
  public void setCompletedByFullName(String completedByFullName) { this.completedByFullName = completedByFullName; }

  public LocalDateTime getCompletedAt() { return completedAt; }
  public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}