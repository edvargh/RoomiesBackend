package com.roomies.dto.user;

import jakarta.validation.constraints.Size;

/** Fields the current user is allowed to edit on their own profile. */
public class UserUpdateRequestDto {

  // Optional; update only if provided
  @Size(min = 1, max = 100)
  private String displayName;

  // Optional; to change password you must provide both currentPassword and newPassword
  @Size(min = 6, max = 100)
  private String currentPassword;

  @Size(min = 8, max = 100)
  private String newPassword;

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }

  public String getCurrentPassword() { return currentPassword; }
  public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

  public String getNewPassword() { return newPassword; }
  public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
