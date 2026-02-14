package com.roomies.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "task_responsibles",
    uniqueConstraints = @UniqueConstraint(name = "uk_task_user", columnNames = {"task_id", "user_id"})
)
public class TaskResponsible {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "responsible_id", updatable = false, nullable = false)
  private Long responsibleId;

  @ManyToOne(optional = false)
  @JoinColumn(name = "task_id",
      foreignKey = @ForeignKey(name = "fk_responsible_task"))
  private Task task;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id",
      foreignKey = @ForeignKey(name = "fk_responsible_user"))
  private User user;

  /** Rotation order: 1 = next in line when rotation = SINGLE. */
  @Column(nullable = false)
  private int position = 1;

  public TaskResponsible() {}
  public TaskResponsible(Task task, User user, int position) {
    this.task     = task;
    this.user     = user;
    this.position = position;
  }

  /* getters / setters */

  public Long getResponsibleId()      { return responsibleId; }

  public Task getTask()               { return task; }
  public void setTask(Task t)         { this.task = t; }

  public User getUser()               { return user; }
  public void setUser(User u)         { this.user = u; }

  public int getPosition()            { return position; }
  public void setPosition(int p)      { this.position = p; }
}

