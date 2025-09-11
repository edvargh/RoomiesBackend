package com.roomies.dto.task;

import com.roomies.entity.Frequency;
import com.roomies.entity.Rotation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TaskUpdateRequestDto {

  @NotBlank(message = "Description is required")
  @Size(max = 255)
  private String description;

  @NotNull(message = "Frequency is required")
  private Frequency frequency;

  @NotNull(message = "Rotation is required")
  private Rotation rotation;

  @NotNull(message = "Start date is required")
  private LocalDate startDate;

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
}
