package com.roomies.controller;

import com.roomies.dto.TaskCreateRequestDto;
import com.roomies.dto.TaskResponseDto;
import com.roomies.dto.TaskUpdateRequestDto;
import com.roomies.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;
  private static final String MESSAGE_KEY = "message";

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  /** Creates a new task in the authenticated user's household. */
  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<Map<String, String>> createTask(
      @Valid @RequestBody TaskCreateRequestDto dto,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    taskService.createTask(dto, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Task created successfully"));
  }

  /** Retrieves all tasks for the authenticated user's household. */
  @PreAuthorize("isAuthenticated()")
  @GetMapping
  public ResponseEntity<List<TaskResponseDto>> getTasks(
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(taskService.getTasksForHousehold(userDetails.getUsername()));
  }

  // TaskController.java
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{id}")
  public ResponseEntity<TaskResponseDto> getTask(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    return ResponseEntity.ok(taskService.getTaskById(id, userDetails.getUsername()));
  }

  /** Updates an existing task (must be in the same household). */
  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{id}")
  public ResponseEntity<Map<String, String>> updateTask(
      @PathVariable Long id,
      @Valid @RequestBody TaskUpdateRequestDto dto,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    taskService.updateTask(id, dto, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Task updated successfully"));
  }

  /** Marks a task as completed by the authenticated user and advances schedule/rotation. */
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{id}/complete")
  public ResponseEntity<Map<String, String>> completeTask(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    taskService.completeTask(id, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Task completed"));
  }

  /** Deletes a task (must be in the same household). */
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteTask(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails
  ) {
    taskService.deleteTask(id, userDetails.getUsername());
    return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Task deleted successfully"));
  }
}
