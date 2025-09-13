package com.roomies.service;

import com.roomies.dto.task.TaskCreateRequestDto;
import com.roomies.dto.task.TaskResponseDto;
import com.roomies.dto.task.TaskUpdateRequestDto;
import com.roomies.dto.task.TaskLogResponseDto;
import com.roomies.entity.*;
import com.roomies.repository.*;
import com.roomies.service.util.TaskMapper;
import com.roomies.service.util.TaskSchedule;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    // Step 1: fetch tasks
    List<Task> tasks = taskRepo.findByHousehold_HouseholdIdOrderByNextDueAsc(hhId);
    if (tasks.isEmpty()) return List.of();

    // Step 2: collect IDs and batch-fetch all responsibles in one query
    List<Long> taskIds = tasks.stream().map(Task::getTaskId).toList();

    List<TaskResponsible> allResponsibles =
        respRepo.findAllByTask_TaskIdInOrderByTask_TaskIdAscPositionAsc(taskIds);

    // Step 3: group responsibles by taskId, preserving order
    Map<Long, List<TaskResponsible>> byTaskId = new LinkedHashMap<>();
    for (TaskResponsible tr : allResponsibles) {
      Long tid = tr.getTask().getTaskId();
      byTaskId.computeIfAbsent(tid, k -> new ArrayList<>()).add(tr);
    }

    // Step 4: map to DTOs using pre-grouped lists (no extra queries)
    return tasks.stream()
        .map(t -> TaskMapper.toDto(t, byTaskId.getOrDefault(t.getTaskId(), List.of())))
        .toList();
  }

  /**
   * Retrieves a specific task by ID; must belong to the authenticated user's household.
   */
  @Transactional(readOnly = true)
  public TaskResponseDto getTaskById(Long taskId, String email) {
    Task task = getAuthorizedTask(taskId, email);
    log.debug("Retrieving task {} for household {}", taskId, task.getHousehold().getHouseholdId());
    return TaskMapper.toDto(task, respRepo.findAllByTask_TaskIdOrderByPositionAsc(task.getTaskId()));
  }

  /**
   * Retrieves paginated task completion logs for the authenticated user's household.
   */
  @Transactional(readOnly = true)
  public List<TaskLogResponseDto> getTaskLogs(String email, int page, int size) {
    User user = getAuthenticatedUser(email);
    Household household = user.getHousehold();
    if (household == null) {
      throw new IllegalStateException("User must be part of a household");
    }

    // Guardrails on size (e.g., max 100)
    int safePage = Math.max(0, page);
    int safeSize = Math.min(Math.max(1, size), 100);

    var pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "completedAt"));
    var logsPage = logRepo.findByTask_Household_HouseholdId(household.getHouseholdId(), pageable);

    return TaskLogResponseDto.fromEntities(logsPage.getContent());
  }

  /**
   * Updates a task; must belong to the authenticated user's household.
   */
  @Transactional
  public void updateTask(Long taskId, TaskUpdateRequestDto dto, String email) {
    Task task = getAuthorizedTask(taskId, email);

    updateCoreFields(task, dto);

    if (dto.getResponsibleUserIds() != null) {
      replaceResponsibles(task, dto.getResponsibleUserIds());
    }

    log.debug("Updated task with ID: {}", taskId);
  }

  /** Core field updates; no branching except a single null-check. */
  private void updateCoreFields(Task task, TaskUpdateRequestDto dto) {
    task.setDescription(dto.getDescription());
    task.setFrequency(dto.getFrequency());
    task.setRotation(dto.getRotation());
    task.setStartDate(dto.getStartDate());

    if (task.getNextDue() == null) {
      task.setNextDue(TaskSchedule.firstDue(dto.getStartDate()));
    }
  }

  /** Replace + reorder responsibles, validating household membership. */
  private void replaceResponsibles(Task task, List<Long> userIds) {
    List<Long> ordered = dedupePreservingOrder(userIds);
    requireNonEmpty(ordered);

    Long householdId = task.getHousehold().getHouseholdId();
    Map<Long, User> usersById = loadAndValidateUsers(ordered, householdId);

    List<TaskResponsible> current = respRepo.findAllByTask_TaskIdOrderByPositionAsc(task.getTaskId());
    Map<Long, TaskResponsible> currentByUserId = indexByUserId(current);

    removeUnlistedResponsibles(current, ordered);
    upsertAndReorderResponsibles(task, ordered, usersById, currentByUserId);
  }

  private List<Long> dedupePreservingOrder(List<Long> ids) {
    if (ids == null) return List.of();
    List<Long> out = new ArrayList<>(ids.size());
    Set<Long> seen = new HashSet<>();
    for (Long id : ids) {
      if (id != null && seen.add(id)) out.add(id);
    }
    return out;
  }

  private void requireNonEmpty(List<?> list) {
    if (list.isEmpty()) throw new IllegalArgumentException("At least one responsible user is required");
  }

  /** Load users and enforce same-household membership. */
  private Map<Long, User> loadAndValidateUsers(List<Long> ids, Long householdId) {
    Map<Long, User> users = new HashMap<>(ids.size());
    for (Long uid : ids) {
      User u = userRepo.findById(uid)
          .orElseThrow(() -> new EntityNotFoundException("User not found: " + uid));
      if (u.getHousehold() == null || !householdId.equals(u.getHousehold().getHouseholdId())) {
        throw new AccessDeniedException("Responsible must be in the same household");
      }
      users.put(uid, u);
    }
    return users;
  }

  private Map<Long, TaskResponsible> indexByUserId(List<TaskResponsible> list) {
    Map<Long, TaskResponsible> map = new HashMap<>(list.size());
    for (TaskResponsible tr : list) {
      map.put(tr.getUser().getUserId(), tr);
    }
    return map;
  }

  private void removeUnlistedResponsibles(List<TaskResponsible> current, List<Long> orderedIds) {
    List<TaskResponsible> toRemove = new ArrayList<>();
    Set<Long> keep = new HashSet<>(orderedIds);
    for (TaskResponsible tr : current) {
      if (!keep.contains(tr.getUser().getUserId())) toRemove.add(tr);
    }
    if (!toRemove.isEmpty()) respRepo.deleteAll(toRemove);
  }

  /** Create missing responsibles and normalize positions to 1.N. */
  private void upsertAndReorderResponsibles(
      Task task,
      List<Long> orderedIds,
      Map<Long, User> usersById,
      Map<Long, TaskResponsible> currentByUserId
  ) {
    List<TaskResponsible> changes = new ArrayList<>();
    int pos = 1;

    for (Long uid : orderedIds) {
      TaskResponsible existing = currentByUserId.get(uid);
      if (existing == null) {
        changes.add(new TaskResponsible(task, usersById.get(uid), pos));
      } else if (existing.getPosition() != pos) {
        existing.setPosition(pos);
        changes.add(existing);
      }
      pos++;
    }

    if (!changes.isEmpty()) respRepo.saveAll(changes);
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

    // Determine current due (fallback to first due if nextDue is not set)
    LocalDateTime due = (task.getNextDue() != null)
        ? task.getNextDue()
        : TaskSchedule.firstDue(task.getStartDate());

    // Block early completion (compare by date only, ignore time)
    if (LocalDate.now().isBefore(due.toLocalDate())) {
      throw new IllegalStateException("Task cannot be completed before its due date");
    }

    User user = getAuthenticatedUser(email);
    // Write completion log
    logRepo.save(new TaskLog(task, user, java.time.Instant.now()));

    // Advance schedule
    if (task.getFrequency() == Frequency.ONCE) {
      task.setNextDue(null); // terminal
    } else {
      task.setNextDue(TaskSchedule.nextAfter(due, task.getFrequency()));
    }

    rotateIfSingle(task);

    log.debug("Completed task {} by user {}", taskId, user.getUserId());
  }

  private void rotateIfSingle(Task task) {
    if (task.getRotation() != Rotation.SINGLE) return;

    List<TaskResponsible> rs =
        respRepo.findAllByTask_TaskIdOrderByPositionAsc(task.getTaskId());

    if (rs.size() < 2) return;

    int n = rs.size();
    for (TaskResponsible tr : rs) {
      int pos = tr.getPosition();
      tr.setPosition(pos == 1 ? n : pos - 1);
    }
    respRepo.saveAll(rs);
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
