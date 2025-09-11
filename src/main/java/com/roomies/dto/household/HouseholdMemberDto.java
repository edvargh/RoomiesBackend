package com.roomies.dto.household;

import com.roomies.entity.User;

public class HouseholdMemberDto {
  private Long userId;
  private String displayName;
  private String email;

  public HouseholdMemberDto() {}

  public HouseholdMemberDto(Long userId, String displayName, String email) {
    this.userId = userId;
    this.displayName = displayName;
    this.email = email;
  }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public static HouseholdMemberDto fromEntity(User u) {
    return new HouseholdMemberDto(u.getUserId(), u.getDisplayName(), u.getEmail());
  }
}
