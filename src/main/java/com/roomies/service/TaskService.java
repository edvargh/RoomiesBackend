package com.roomies.service;

import com.roomies.dto.TaskCreateRequestDto;
import com.roomies.dto.TaskResponseDto;
import com.roomies.dto.TaskUpdateRequestDto;
import com.roomies.entity.*;
import com.roomies.repository.*;
import com.roomies.service.util.TaskMapper;
import com.roomies.service.util.TaskSchedule;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Service for handling task related operations. */
@Service
public class TaskService {

  private static final Logger log = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepo;
  private final TaskResponsibleRepository respRepo;
  private final TaskLogRepository logRepo;
  private final UserRepository userRepo;

  public TaskService(TaskRepository taskRepo,
      TaskResponsibleRepository respRepo,
      TaskLogRepository logRepo,
      UserRepository userRepo) {
    this.taskRepo = taskRepo;
    this.respRepo = respRepo;
    this.logRepo = logRepo;
    this.userRepo = userRepo;
  }

  /**
   * Creates a new task for the authenticated user's household.
   */
  @Transactional
  public void createTask(TaskCreateRequestDto dto, String email) {
    User user = getAuthenticatedUser(email);
    Household household = user.getHousehold();
    if (household == null) {
      throw new IllegalStateException("User must be part of a household");
    }
    if (dto.getResponsibleUserIds() == null || dto.getResponsibleUserIds().isEmpty()) {
      throw new IllegalArgumentException("At least one responsible user is required");
    }

    Task task = new Task();
    task.setHousehold(household);
    task.setDescription(dto.getDescription());
    task.setFrequency(dto.getFrequency());
    task.setRotation(dto.getRotation());
    task.setStartDate(dto.getStartDate());
    task.setNextDue(TaskSchedule.firstDue(dto.getStartDate()));

    log.debug("Creating task '{}' for household {}", dto.getDescription(), household.getHouseholdId());
    Task saved = taskRepo.save(task);

    // Create responsibles in given order
    int pos = 1;
    for (Long uid : dto.getResponsibleUserIds()) {
      User u = userRepo.findById(uid)
          .orElseThrow(() -> new EntityNotFoundException("User not found: " + uid));

      if (u.getHousehold() == null ||
          !u.getHousehold().getHouseholdId().equals(household.getHouseholdId())) {
        throw new AccessDeniedException("Responsible must be in the same household");
      }

      respRepo.save(new TaskResponsible(saved, u, pos++));
    }
    log.debug("Task created with ID: {}", saved.getTaskId());
  }

  /**
   * Retrieves all tasks for the authenticated user's household.
   */
  @Transactional(readOnly = true)
  public List<TaskResponseDto> getTasksForHousehold(String email) {
    User user = getAuthenticatedUser(email);
    Household household = user.getHousehold();
    if (household == null) {
      throw new IllegalStateException("User must be part of a household");
    }
    Long hhId = household.getHouseholdId();
    log.debug("Retrieving tasks for household ID: {}", hhId);

    return taskRepo.findByHousehold_HouseholdIdOrderByNextDueAsc(hhId).stream()
        .map(task -> TaskMapper.toDto(task,
            respRepo.findAllByTask_TaskIdOrderByPositionAsc(task.getTaskId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public TaskResponseDto getTaskById(Long taskId, String email) {
    Task task = getAuthorizedTask(taskId, email);
    log.debug("Retrieving task {} for household {}", taskId, task.getHousehold().getHouseholdId());
    return TaskMapper.toDto(task, respRepo.findAllByTask_TaskIdOrderByPositionAsc(task.getTaskId()));
  }

  /**
   * Updates a task; must belong to the authenticated user's household.
   */
  @Transactional
  public void updateTask(Long taskId, TaskUpdateRequestDto dto, String email) {
    Task task = getAuthorizedTask(taskId, email);

    task.setDescription(dto.getDescription());
    task.setFrequency(dto.getFrequency());
    task.setRotation(dto.getRotation());
    task.setStartDate(dto.getStartDate());

    if (task.getNextDue() == null) {
      task.setNextDue(TaskSchedule.firstDue(dto.getStartDate()));
    }
    log.debug("Updated task with ID: {}", taskId);
  }

  /**
   * Marks a task as completed by the authenticated user, advances schedule, and rotates if SINGLE.
   */
  @Transactional
  public void completeTask(Long taskId, String email) {
    Task task = getAuthorizedTask(taskId, email);
    if (task.getFrequency() == Frequency.ONCE && task.getNextDue() == null) {
      throw new IllegalStateException("Task already completed");
    }
    User user = getAuthenticatedUser(email);

    // Write completion log
    logRepo.save(new TaskLog(task, user));

    // Advance schedule
    LocalDateTime due = task.getNextDue() != null
        ? task.getNextDue()
        : TaskSchedule.firstDue(task.getStartDate());

    if (task.getFrequency() == Frequency.ONCE) {
      task.setNextDue(null); // terminal
    } else {
      task.setNextDue(TaskSchedule.nextAfter(due, task.getFrequency()));
    }

    // Rotate if SINGLE
    if (task.getRotation() == Rotation.SINGLE) {
      List<TaskResponsible> list = respRepo.findAllByTask_TaskIdOrderByPositionAsc(task.getTaskId());
      if (list.size() > 1) {
        TaskResponsible first = list.remove(0);
        list.add(first);
        int p = 1;
        for (TaskResponsible tr : list) tr.setPosition(p++);
        respRepo.saveAll(list);
      }
    }

    log.debug("Completed task {} by user {}", taskId, user.getUserId());
  }

  /**
   * Deletes a task; must belong to the authenticated user's household.
   */
  @Transactional
  public void deleteTask(Long taskId, String email) {
    Task task = getAuthorizedTask(taskId, email);
    taskRepo.delete(task);
    log.debug("Deleted task with ID: {}", taskId);
  }

  /* ---------- Private helpers ---------- */

  private Task getAuthorizedTask(Long taskId, String email) {
    Task task = taskRepo.findById(taskId)
        .orElseThrow(() -> new EntityNotFoundException("Task not found"));
    User user = getAuthenticatedUser(email);
    if (user.getHousehold() == null ||
        !task.getHousehold().getHouseholdId().equals(user.getHousehold().getHouseholdId())) {
      throw new AccessDeniedException("Access denied: not in same household");
    }
    return task;
  }

  private User getAuthenticatedUser(String email) {
    return userRepo.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
  }
}
