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
  @JoinColumn(name = "collective_id", referencedColumnName = "collective_id", foreignKey = @ForeignKey(name = "fk_users_collective"))
  private Collective collective;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "ENUM('member','admin') DEFAULT 'member'")
  private Role role = Role.MEMBER;

  @Column(nullable = false)
  private boolean confirmed = false;

  @Column(name = "confirmation_token", length = 100)
  private String confirmationToken;

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

  public Collective getCollective() {
    return collective;
  }

  public void setCollective(Collective collective) {
    this.collective = collective;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
