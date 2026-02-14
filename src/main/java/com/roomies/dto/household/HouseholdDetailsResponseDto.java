package com.roomies.dto.household;

import com.roomies.entity.Household;
import com.roomies.entity.User;
import java.util.List;

public class HouseholdDetailsResponseDto {

  private Long householdId;
  private String name;
  private String joinCode;
  private List<HouseholdMemberDto> members;

  public HouseholdDetailsResponseDto() {}

  public HouseholdDetailsResponseDto(Long householdId, String name, String joinCode,
      List<HouseholdMemberDto> members) {
    this.householdId = householdId;
    this.name = name;
    this.joinCode = joinCode;
    this.members = members;
  }

  public Long getHouseholdId() { return householdId; }
  public void setHouseholdId(Long householdId) { this.householdId = householdId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getJoinCode() { return joinCode; }
  public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

  public List<HouseholdMemberDto> getMembers() { return members; }
  public void setMembers(List<HouseholdMemberDto> members) { this.members = members; }

  public static HouseholdDetailsResponseDto fromEntity(Household h, List<User> members) {
    return new HouseholdDetailsResponseDto(
        h.getHouseholdId(),
        h.getName(),
        h.getJoinCode(),
        members.stream().map(HouseholdMemberDto::fromEntity).toList()
    );
  }
}
