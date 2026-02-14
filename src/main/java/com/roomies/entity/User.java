package com.roomies.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * The User entity represents a user in the system, associated with a collective.
 */
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id", nullable = false, updatable = false)
  private Long userId;

  @Column(nullable = false, unique = true, length = 200)
  private String email;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(nullable = false, length = 60)
  private String password;

  @ManyToOne
  @JoinColumn(name = "household_id", referencedColumnName = "household_id", foreignKey = @ForeignKey(name = "fk_users_household"))
  private Household household;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "ENUM('MEMBER','ADMIN') DEFAULT 'MEMBER'")
  private Role role = Role.MEMBER;

  @Column(nullable = false)
  private boolean confirmed = false;

  @Column(name = "confirmation_token", length = 100)
  private String confirmationToken;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime createdAt;

  public User() {
    // Default constructor
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Household getHousehold() {
    return household;
  }

  public void setHousehold(Household household) {
    this.household = household;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public void setConfirmed(boolean confirmed) {
    this.confirmed = confirmed;
  }

  public String getConfirmationToken() {
    return confirmationToken;
  }

  public void setConfirmationToken(String confirmationToken) {
    this.confirmationToken = confirmationToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
