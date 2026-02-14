package com.roomies.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for refresh token requests.
 * Contains the refresh token to be used for obtaining a new access token.
 */
public class RefreshTokenRequestDto {

  @NotBlank
  private String refreshToken;

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
