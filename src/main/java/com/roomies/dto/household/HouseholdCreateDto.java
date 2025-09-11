package com.roomies.dto.household;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a household.
 * Contains a name field with validation constraints.
 */
public class HouseholdCreateDto {

  @NotBlank
  @Size(max=100)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
