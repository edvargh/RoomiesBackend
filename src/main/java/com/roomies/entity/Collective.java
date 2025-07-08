package com.roomies.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The Collective entity represents a group of users sharing a common space or interest.
 */
@Entity
@Table(name = "collectives")
public class Collective {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "collective_id", nullable = false, updatable = false)
  private Long collectiveId;

  @Column(nullable = false, length = 100)
  private String name;

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

  @OneToMany(mappedBy = "collective")
  private List<User> users = new ArrayList<>();

  public Collective() {
    // Default constructor
  }

  public Long getCollectiveId() {
    return collectiveId;
  }

  public void setCollectiveId(Long collectiveId) {
    this.collectiveId = collectiveId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public List<User> getUsers() {
    return users;
  }
}
