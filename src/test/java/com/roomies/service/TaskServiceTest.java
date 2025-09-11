package com.roomies.service;

import com.roomies.dto.task.TaskCreateRequestDto;
import com.roomies.dto.task.TaskResponseDto;
import com.roomies.dto.task.TaskUpdateRequestDto;
import com.roomies.entity.*;
import com.roomies.repository.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepo;
  @Mock private TaskResponsibleRepository respRepo;
  @Mock private TaskLogRepository logRepo;
  @Mock private UserRepository userRepo;

  @InjectMocks private TaskService taskService;

  @Nested
  class CreateTask {
    @Test
    void shouldCreateTaskWhenUserAndResponsiblesInSameHousehold() {
      String email = "user@example.com";

      Household household = new Household();
      household.setHouseholdId(1L);

      User creator = new User();
      creator.setEmail(email);
      creator.setHousehold(household);

      User responsible = new User();
      responsible.setUserId(2L);
      responsible.setHousehold(household);

      TaskCreateRequestDto dto = new TaskCreateRequestDto();
      dto.setDescription("Take out trash");
      dto.setFrequency(Frequency.DAILY);
      dto.setRotation(Rotation.SINGLE);
      dto.setStartDate(LocalDate.now());
      dto.setResponsibleUserIds(List.of(2L));

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(creator));
      when(userRepo.findById(2L)).thenReturn(Optional.of(responsible));
      when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

      assertDoesNotThrow(() -> taskService.createTask(dto, email));

      verify(taskRepo).save(any(Task.class));
      verify(respRepo).save(any(TaskResponsible.class));
    }

    @Test
    void shouldThrowWhenResponsibleNotInSameHousehold() {
      String email = "user@example.com";

      Household h1 = new Household(); h1.setHouseholdId(1L);
      Household h2 = new Household(); h2.setHouseholdId(2L);

      User creator = new User(); creator.setEmail(email); creator.setHousehold(h1);
      User outsider = new User(); outsider.setUserId(99L); outsider.setHousehold(h2);

      TaskCreateRequestDto dto = new TaskCreateRequestDto();
      dto.setDescription("Task");
      dto.setFrequency(Frequency.ONCE);
      dto.setRotation(Rotation.SINGLE);
      dto.setStartDate(LocalDate.now());
      dto.setResponsibleUserIds(List.of(99L));

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(creator));
      when(userRepo.findById(99L)).thenReturn(Optional.of(outsider));
      when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

      assertThrows(AccessDeniedException.class, () -> taskService.createTask(dto, email));
    }
  }

  @Nested
  class CompleteTask {
    @Test
    void shouldMarkOnceTaskAsCompleted() {
      String email = "user@example.com";

      Household household = new Household(); household.setHouseholdId(1L);

      User user = new User(); user.setUserId(10L); user.setEmail(email); user.setHousehold(household);

      Task task = new Task();
      task.setTaskId(5L);
      task.setHousehold(household);
      task.setFrequency(Frequency.ONCE);
      task.setRotation(Rotation.SINGLE);
      task.setStartDate(LocalDate.now().minusDays(1));
      task.setNextDue(LocalDateTime.now());

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findById(5L)).thenReturn(Optional.of(task));

      assertDoesNotThrow(() -> taskService.completeTask(5L, email));

      verify(logRepo).save(any(TaskLog.class));
      assertNull(task.getNextDue(), "Once task should have null nextDue after completion");
    }

    @Test
    void shouldThrowIfOnceTaskAlreadyCompleted() {
      String email = "user@example.com";

      Household household = new Household(); household.setHouseholdId(1L);

      User user = new User(); user.setUserId(10L); user.setEmail(email); user.setHousehold(household);

      Task task = new Task();
      task.setTaskId(5L);
      task.setHousehold(household);
      task.setFrequency(Frequency.ONCE);
      task.setNextDue(null); // already completed

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findById(5L)).thenReturn(Optional.of(task));

      assertThrows(IllegalStateException.class, () -> taskService.completeTask(5L, email));
    }
  }

  @Nested
  class DeleteTask {
    @Test
    void shouldDeleteTaskWhenUserInSameHousehold() {
      String email = "user@example.com";
      Household household = new Household(); household.setHouseholdId(1L);

      User user = new User(); user.setEmail(email); user.setHousehold(household);

      Task task = new Task(); task.setTaskId(20L); task.setHousehold(household);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findById(20L)).thenReturn(Optional.of(task));

      assertDoesNotThrow(() -> taskService.deleteTask(20L, email));

      verify(taskRepo).delete(task);
    }

    @Test
    void shouldThrowAccessDeniedIfNotSameHousehold() {
      String email = "user@example.com";
      Household h1 = new Household(); h1.setHouseholdId(1L);
      Household h2 = new Household(); h2.setHouseholdId(2L);

      User user = new User(); user.setEmail(email); user.setHousehold(h1);

      Task task = new Task(); task.setTaskId(20L); task.setHousehold(h2);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findById(20L)).thenReturn(Optional.of(task));

      assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(20L, email));

      verify(taskRepo, never()).delete(any());
    }
  }

  @Nested
  class UpdateTask {
    @Test
    void shouldUpdateTaskWhenAuthorized() {
      String email = "user@example.com";
      Household household = new Household(); household.setHouseholdId(1L);

      User user = new User(); user.setEmail(email); user.setHousehold(household);

      Task task = new Task(); task.setTaskId(30L); task.setHousehold(household);

      TaskUpdateRequestDto dto = new TaskUpdateRequestDto();
      dto.setDescription("Updated desc");
      dto.setFrequency(Frequency.WEEKLY);
      dto.setRotation(Rotation.SINGLE);
      dto.setStartDate(LocalDate.now());

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findById(30L)).thenReturn(Optional.of(task));

      assertDoesNotThrow(() -> taskService.updateTask(30L, dto, email));

      assertEquals("Updated desc", task.getDescription());
      assertEquals(Frequency.WEEKLY, task.getFrequency());
    }
  }

  @Nested
  class GetTasksForHousehold {

    @Test
    void shouldReturnMappedDtosForUsersHousehold() {
      String email = "user@example.com";

      Household household = new Household();
      household.setHouseholdId(1L);

      User user = new User();
      user.setEmail(email);
      user.setDisplayName("Alice");
      user.setHousehold(household);

      Task task = new Task();
      task.setTaskId(11L);
      task.setHousehold(household);
      task.setDescription("Clean kitchen");
      task.setFrequency(Frequency.DAILY);
      task.setRotation(Rotation.SINGLE);
      task.setStartDate(LocalDate.now());

      TaskResponsible resp = new TaskResponsible();
      resp.setTask(task);
      resp.setUser(user);
      resp.setPosition(1);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findByHousehold_HouseholdIdOrderByNextDueAsc(1L)).thenReturn(List.of(task));
      when(respRepo.findAllByTask_TaskIdOrderByPositionAsc(11L)).thenReturn(List.of(resp));

      // Act
      List<TaskResponseDto> result = taskService.getTasksForHousehold(email);

      // Assert
      assertEquals(1, result.size());
      TaskResponseDto dto = result.get(0);
      assertEquals("Clean kitchen", dto.getDescription());
      assertEquals(Frequency.DAILY, dto.getFrequency());
      assertEquals(1, dto.getResponsibles().size());
      assertEquals("Alice", dto.getResponsibles().get(0).getFullName());
    }
  }

  @Nested
  class GetTaskById {
    @Test
    void shouldReturnSingleMappedTask() {
      String email = "user@example.com";
      Household household = new Household(); household.setHouseholdId(1L);

      User user = new User(); user.setEmail(email); user.setDisplayName("Bob"); user.setHousehold(household);

      Task task = new Task();
      task.setTaskId(77L);
      task.setHousehold(household);
      task.setDescription("Vacuum living room");
      task.setFrequency(Frequency.WEEKLY);
      task.setRotation(Rotation.SINGLE);
      task.setStartDate(LocalDate.now());

      TaskResponsible resp = new TaskResponsible();
      resp.setTask(task);
      resp.setUser(user);
      resp.setPosition(1);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findById(77L)).thenReturn(Optional.of(task));
      when(respRepo.findAllByTask_TaskIdOrderByPositionAsc(77L)).thenReturn(List.of(resp));

      // Act
      TaskResponseDto dto = taskService.getTaskById(77L, email);

      // Assert
      assertEquals("Vacuum living room", dto.getDescription());
      assertEquals(Frequency.WEEKLY, dto.getFrequency());
      assertEquals(1, dto.getResponsibles().size());
      assertEquals("Bob", dto.getResponsibles().get(0).getFullName());
    }
  }

}
