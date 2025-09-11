package com.roomies.dto.household;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinHouseholdRequestDto {

  @NotBlank
  @Size(max = 6)
  private String joinCode;

  public String getJoinCode() {
    return joinCode;
  }

  public void setJoinCode(String joinCode) {
    this.joinCode = joinCode;
  }
}
