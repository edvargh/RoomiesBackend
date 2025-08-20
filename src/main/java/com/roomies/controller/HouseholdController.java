package com.roomies.controller;

import com.roomies.dto.HouseholdCreateDto;
import com.roomies.dto.HouseholdDetailsResponseDto;
import com.roomies.dto.JoinHouseholdRequestDto;
import com.roomies.service.HouseholdService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/household")
public class HouseholdController {

  private final HouseholdService householdService;

  public HouseholdController(HouseholdService householdService) {
    this.householdService = householdService;
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/create")
  public ResponseEntity<Map<String, String>> createHousehold(
      @Valid @RequestBody HouseholdCreateDto dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    householdService.createHousehold(dto, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(
        "message", "Household created successfully"));
  }

  @PostMapping("/join")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, String>> joinHousehold(
      @Valid @RequestBody JoinHouseholdRequestDto dto,
      @AuthenticationPrincipal UserDetails userDetails) {
    householdService.joinHousehold(dto, userDetails.getUsername());
    return ResponseEntity.ok(Map.of("message", "Successfully joined household"));
  }

  /**
   * Returns the authenticated user's household details: id, name, joinCode, members.
   */
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ResponseEntity<HouseholdDetailsResponseDto> getMyHousehold(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(householdService.getMyHouseholdDetails(userDetails.getUsername()));
  }
}
