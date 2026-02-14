package com.roomies.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * The Task entity represents a recurring or one‑off household chore.
 */
@Entity
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_tasks_next_due", columnList = "next_due"),
        @Index(name = "idx_tasks_household", columnList = "household_id")
    }
)
public class Task {

  /* ---------- PK & FK ---------- */

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "task_id", nullable = false, updatable = false)
  private Long taskId;

  @ManyToOne(optional = false)
  @JoinColumn(
      name = "household_id",
      foreignKey = @ForeignKey(name = "fk_tasks_household")
  )
  private Household household;

  /* ---------- Core fields ---------- */

  @Column(nullable = false, length = 255)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Frequency frequency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Rotation rotation = Rotation.SINGLE;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "next_due", nullable = false)
  private LocalDateTime nextDue;

  /* ---------- Audit columns ---------- */

  @Column(
      name = "created_at",
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP",
      insertable = false,
      updatable = false
  )
  private LocalDateTime createdAt;

  @Column(
      name = "updated_at",
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP",
      insertable = false,
      updatable = false
  )
  private LocalDateTime updatedAt;

  /* ---------- Relationships ---------- */

  /**
   * Users responsible for this task.
   * `TaskResponsible` is a join‑entity that also stores the rotation position.
   */
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<TaskResponsible> responsibles;

  /**
   * History of completions.
   */
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<TaskLog> logs;

  /* ---------- Constructors ---------- */

  public Task() {
    // JPA requires a no‑arg constructor
  }

  /* ---------- Getters & setters ---------- */

  public Long getTaskId()                      { return taskId; }
  public void setTaskId(Long id)               { this.taskId = id; }

  public Household getHousehold()              { return household; }
  public void setHousehold(Household h)        { this.household = h; }

  public String getDescription()               { return description; }
  public void setDescription(String d)         { this.description = d; }

  public Frequency getFrequency()              { return frequency; }
  public void setFrequency(Frequency f)        { this.frequency = f; }

  public Rotation getRotation()                { return rotation; }
  public void setRotation(Rotation r)          { this.rotation = r; }

  public LocalDate getStartDate()              { return startDate; }
  public void setStartDate(LocalDate d)        { this.startDate = d; }

  public LocalDateTime getNextDue()            { return nextDue; }
  public void setNextDue(LocalDateTime d)      { this.nextDue = d; }

  public LocalDateTime getCreatedAt()          { return createdAt; }
  public LocalDateTime getUpdatedAt()          { return updatedAt; }

  public Set<TaskResponsible> getResponsibles(){ return responsibles; }
  public void setResponsibles(Set<TaskResponsible> s){ this.responsibles = s; }

  public Set<TaskLog> getLogs()                { return logs; }
  public void setLogs(Set<TaskLog> l)          { this.logs = l; }
}
