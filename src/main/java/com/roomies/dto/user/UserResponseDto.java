package com.roomies.dto.user;

import com.roomies.entity.User;
import com.roomies.entity.Household;
import com.roomies.entity.Role;

public class UserResponseDto {

  private Long userId;
  private String email;
  private String displayName;
  private Long householdId;
  private String householdName;
  private Role role;

  public UserResponseDto() {}

  public UserResponseDto(Long userId, String email, String displayName, Long householdId, String householdName, Role role) {
    this.userId = userId;
    this.email = email;
    this.displayName = displayName;
    this.householdId = householdId;
    this.householdName = householdName;
    this.role = role;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Long getHouseholdId() {
    return householdId;
  }

  public void setHouseholdId(Long householdId) {
    this.householdId = householdId;
  }

  public String getHouseholdName() {
    return householdName;
  }

  public void setHouseholdName(String householdName) {
    this.householdName = householdName;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public static UserResponseDto fromEntity(User user) {
    Household household = user.getHousehold();
    Long householdId = (household != null) ? household.getHouseholdId() : null;
    String householdName = (household != null) ? household.getName() : null;

    return new UserResponseDto(
        user.getUserId(),
        user.getEmail(),
        user.getDisplayName(),
        householdId,
        householdName,
        user.getRole()
    );
  }
}
