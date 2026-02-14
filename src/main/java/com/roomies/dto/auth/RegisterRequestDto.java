package com.roomies.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests.
 * Contains fields for display name, email, and password with validation constraints.
 */
public class RegisterRequestDto {

  @NotBlank
  @Size(max = 100)
  private String displayName;

  @NotBlank
  @Email
  @Size(max = 200)
  private String email;

  @NotBlank
  @Size(min = 8, max = 60)
  private String password;

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String d) { this.displayName = d; }

  public String getEmail() { return email; }
  public void setEmail(String e) { this.email = e; }

  public String getPassword() { return password; }
  public void setPassword(String p) { this.password = p; }
}
