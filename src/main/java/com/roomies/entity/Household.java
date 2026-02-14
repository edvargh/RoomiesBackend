package com.roomies.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * The Household entity represents a group of users sharing a common space or interest.
 */
@Entity
@Table(name = "households")
public class Household {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "household_id", nullable = false, updatable = false)
  private Long householdId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "join_code", nullable = false, unique = true)
  private String joinCode;

  @Column(name = "address_line", length = 200)
  private String addressLine;

  @Column(name = "zip_code", length = 20)
  private String zipCode;

  @Column(length = 100)
  private String city;

  @Column(length = 100)
  private String country;

  @Column(name = "created_at",
      columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP",
      insertable = false,
      updatable = false)
  private LocalDateTime createdAt;

  public Household() {
    // Default constructor
  }

  public Long getHouseholdId() {
    return householdId;
  }

  public void setHouseholdId(Long householdId) {
    this.householdId = householdId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getJoinCode() {
    return joinCode;
  }

  public void setJoinCode(String joinCode) {
    this.joinCode = joinCode;
  }

  public String getAddressLine() {
    return addressLine;
  }

  public void setAddressLine(String addressLine) {
    this.addressLine = addressLine;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
