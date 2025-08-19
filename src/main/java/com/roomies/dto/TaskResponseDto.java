package com.roomies.dto;

import com.roomies.entity.Frequency;
import com.roomies.entity.Rotation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskResponseDto {

  private Long taskId;
  private Long householdId;

  private String description;
  private Frequency frequency;
  private Rotation rotation;

  private LocalDate startDate;
  private LocalDateTime nextDue;

  private List<TaskResponsibleDto> responsibles;

  public TaskResponseDto() {
    // Default constructor
  }

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public Long getHouseholdId() {
    return householdId;
  }

  public void setHouseholdId(Long householdId) {
    this.householdId = householdId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Frequency getFrequency() {
    return frequency;
  }

  public void setFrequency(Frequency frequency) {
    this.frequency = frequency;
  }

  public Rotation getRotation() {
    return rotation;
  }

  public void setRotation(Rotation rotation) {
    this.rotation = rotation;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDateTime getNextDue() {
    return nextDue;
  }

  public void setNextDue(LocalDateTime nextDue) {
    this.nextDue = nextDue;
  }

  public List<TaskResponsibleDto> getResponsibles() {
    return responsibles;
  }

  public void setResponsibles(List<TaskResponsibleDto> responsibles) {
    this.responsibles = responsibles;
  }
}