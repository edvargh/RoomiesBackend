package com.roomies.dto;

public class TaskResponsibleDto {

  private Long userId;
  private String fullName; // optional but handy for UI
  private Integer position;

  public TaskResponsibleDto() {
    // Default constructor
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Integer getPosition() {
    return position;
  }

  public void setPosition(Integer position) {
    this.position = position;
  }
}
