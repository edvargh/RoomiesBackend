package com.roomies.dto.task;

import com.roomies.entity.Frequency;
import com.roomies.entity.Rotation;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class TaskUpdateRequestDto {

  @NotBlank(message = "Description is required")
  @Size(max = 255)
  private String description;

  @NotNull(message = "Frequency is required")
  private Frequency frequency;

  @NotNull(message = "Rotation is required")
  private Rotation rotation;

  @FutureOrPresent(message = "Start date cannot be in the past")
  @NotNull(message = "Start date is required")
  private LocalDate startDate;

  @Nullable
  @Size(min = 1, message = "At least one responsible user is required when provided")
  private List<Long> responsibleUserIds;

  public TaskUpdateRequestDto() {
    // Default constructor
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

  @Nullable
  public List<Long> getResponsibleUserIds() { return responsibleUserIds; }
  public void setResponsibleUserIds(@Nullable List<Long> responsibleUserIds) { this.responsibleUserIds = responsibleUserIds; }
}
