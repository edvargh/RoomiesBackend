package com.roomies.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task_logs",
    indexes = {
        @Index(name = "idx_logs_task", columnList = "task_id"),
        @Index(name = "idx_logs_user", columnList = "completed_by")
    })
public class TaskLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id", updatable = false, nullable = false)
  private Long logId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "task_id",
      foreignKey = @ForeignKey(name = "fk_logs_task"))
  private Task task;

  @ManyToOne(optional = false)
  @JoinColumn(name = "completed_by",
      foreignKey = @ForeignKey(name = "fk_logs_user"))
  private User completedBy;

  @Column(name = "completed_at",
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP",
      insertable = false, updatable = false)
  private Instant completedAt;

  public TaskLog() {}
  public TaskLog(Task task, User user, Instant completedAt) {
    this.task = task;
    this.completedBy = user;
    this.completedAt = completedAt;
  }

  /* getters */

  public Long getLogId()              { return logId; }

  public Task getTask()               { return task; }
  public void setTask(Task t)         { this.task = t; }

  public User getCompletedBy()        { return completedBy; }
  public void setCompletedBy(User u)  { this.completedBy = u; }

  public Instant getCompletedAt(){ return completedAt; }
}
