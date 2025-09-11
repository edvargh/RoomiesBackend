package com.roomies.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for login requests.
 * Contains email and password fields with validation constraints.
 */
public class LoginRequestDto {

  @NotBlank
  @Email
  @Size(max = 200)
  private String email;

  @NotBlank
  @Size(max = 60)
  private String password;

  public String getEmail() { return email; }
  public void setEmail(String e) { this.email = e; }

  public String getPassword() { return password; }
  public void setPassword(String p) { this.password = p; }
}
