package com.roomies.service;

import com.roomies.dto.task.TaskCreateRequestDto;
import com.roomies.dto.task.TaskLogResponseDto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


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
  class UpdateTask {

    private Household hh(long id) {
      Household h = new Household();
      h.setHouseholdId(id);
      return h;
    }

    private User user(long id, String email, Household h) {
      User u = new User();
      u.setUserId(id);
      u.setEmail(email);
      u.setHousehold(h);
      u.setDisplayName("U" + id);
      return u;
    }

    private TaskResponsible tr(Task task, User user, int pos) {
      TaskResponsible r = new TaskResponsible();
      r.setTask(task);
      r.setUser(user);
      r.setPosition(pos);
      return r;
    }

    private TaskUpdateRequestDto baseUpdateDto() {
      TaskUpdateRequestDto dto = new TaskUpdateRequestDto();
      dto.setDescription("Updated desc");
      dto.setFrequency(Frequency.WEEKLY);
      dto.setRotation(Rotation.SINGLE);
      dto.setStartDate(LocalDate.now());
      return dto;
    }

    @Test
    void shouldUpdateCoreFields_andLeaveResponsiblesWhenNull() {
      // Arrange
      String email = "owner@example.com";
      Household h = hh(1L);

      User owner = user(10L, email, h);

      Task task = new Task();
      task.setTaskId(100L);
      task.setHousehold(h);
      task.setDescription("Old");
      task.setFrequency(Frequency.DAILY);
      task.setRotation(Rotation.SINGLE);
      task.setStartDate(LocalDate.now().minusDays(1));
      task.setNextDue(null); // should be initialized

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(owner));
      when(taskRepo.findById(100L)).thenReturn(Optional.of(task));

      TaskUpdateRequestDto dto = baseUpdateDto();
      // important: don't set responsibleUserIds (null) → no changes to responsibles

      // Act
      taskService.updateTask(100L, dto, email);

      // Assert (core fields)
      assertEquals("Updated desc", task.getDescription());
      assertEquals(Frequency.WEEKLY, task.getFrequency());
      assertEquals(Rotation.SINGLE, task.getRotation());
      assertEquals(dto.getStartDate(), task.getStartDate());
      assertNotNull(task.getNextDue(), "nextDue should be initialized when null");

      // Assert (responsibles untouched)
      verify(respRepo, never()).deleteAll(any());
      verify(respRepo, never()).saveAll(any());
    }

    @Test
    void shouldReplaceResponsibles_reorderExisting_andUpsertNew() {
      // Arrange
      String email = "owner@example.com";
      Household h = hh(1L);

      User owner = user(10L, email, h);
      User u1 = user(1L, "u1@x", h);
      User u2 = user(2L, "u2@x", h);
      User u3 = user(3L, "u3@x", h); // new one to add

      Task task = new Task();
      task.setTaskId(200L);
      task.setHousehold(h);
      task.setStartDate(LocalDate.now());

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(owner));
      when(taskRepo.findById(200L)).thenReturn(Optional.of(task));

      // current responsibles: [1(pos1), 2(pos2)]
      when(respRepo.findAllByTask_TaskIdOrderByPositionAsc(200L))
          .thenReturn(List.of(tr(task, u1, 1), tr(task, u2, 2)));

      // new order: [2, 3, 1]
      TaskUpdateRequestDto dto = baseUpdateDto();
      dto.setResponsibleUserIds(List.of(2L, 3L, 1L));

      when(userRepo.findById(1L)).thenReturn(Optional.of(u1));
      when(userRepo.findById(2L)).thenReturn(Optional.of(u2));
      when(userRepo.findById(3L)).thenReturn(Optional.of(u3));

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<TaskResponsible>> saveCap = ArgumentCaptor.forClass(List.class);

      // Act
      taskService.updateTask(200L, dto, email);

      // Assert: delete not called (no removals)
      verify(respRepo, never()).deleteAll(any());

      // Assert: saveAll called with changes (reordered + one new)
      verify(respRepo).saveAll(saveCap.capture());
      List<TaskResponsible> changes = saveCap.getValue();

      // We expect:
      // - existing u2 -> position 1 (was 2)
      // - new u3 -> position 2
      // - existing u1 -> position 3 (was 1)
      assertEquals(3, changes.size());

      // Create a map userId -> pos
      java.util.Map<Long, Integer> posByUser = new java.util.HashMap<>();
      for (TaskResponsible r : changes) {
        posByUser.put(r.getUser().getUserId(), r.getPosition());
        // also confirm all changes target the same task
        assertEquals(task, r.getTask());
      }
      assertEquals(1, posByUser.get(2L));
      assertEquals(2, posByUser.get(3L));
      assertEquals(3, posByUser.get(1L));
    }

    @Test
    void shouldRemoveResponsiblesNotInNewList_andNormalizePositions() {
      // Arrange
      String email = "owner@example.com";
      Household h = hh(1L);

      User owner = user(10L, email, h);
      User u1 = user(1L, "u1@x", h);
      User u2 = user(2L, "u2@x", h);
      User u3 = user(3L, "u3@x", h);

      Task task = new Task();
      task.setTaskId(300L);
      task.setHousehold(h);
      task.setStartDate(LocalDate.now());

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(owner));
      when(taskRepo.findById(300L)).thenReturn(Optional.of(task));

      when(respRepo.findAllByTask_TaskIdOrderByPositionAsc(300L))
          .thenReturn(List.of(tr(task, u1, 1), tr(task, u2, 2), tr(task, u3, 3)));

      // Keep only u2
      TaskUpdateRequestDto dto = baseUpdateDto();
      dto.setResponsibleUserIds(List.of(2L));

      when(userRepo.findById(2L)).thenReturn(Optional.of(u2));

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<TaskResponsible>> deleteCap = ArgumentCaptor.forClass(List.class);
      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<TaskResponsible>> saveCap = ArgumentCaptor.forClass(List.class);

      // Act
      taskService.updateTask(300L, dto, email);

      // Assert: remove u1 and u3
      verify(respRepo).deleteAll(deleteCap.capture());
      List<TaskResponsible> deleted = deleteCap.getValue();
      assertEquals(2, deleted.size());
      java.util.Set<Long> deletedIds = deleted.stream().map(tr -> tr.getUser().getUserId()).collect(java.util.stream.Collectors.toSet());
      assertTrue(deletedIds.containsAll(List.of(1L, 3L)));

      // Assert: u2 position should be normalized to 1 (if not already)
      verify(respRepo).saveAll(saveCap.capture());
      List<TaskResponsible> saved = saveCap.getValue();
      assertEquals(1, saved.size());
      TaskResponsible only = saved.get(0);
      assertEquals(2L, only.getUser().getUserId());
      assertEquals(1, only.getPosition());
    }

    @Test
    void shouldDedupeInputPreservingOrder() {
      // Arrange
      String email = "owner@example.com";
      Household h = hh(1L);

      User owner = user(10L, email, h);
      User u1 = user(1L, "u1@x", h);
      User u2 = user(2L, "u2@x", h);

      Task task = new Task();
      task.setTaskId(400L);
      task.setHousehold(h);
      task.setStartDate(LocalDate.now());

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(owner));
      when(taskRepo.findById(400L)).thenReturn(Optional.of(task));

      // existing: [1(pos1)]
      when(respRepo.findAllByTask_TaskIdOrderByPositionAsc(400L))
          .thenReturn(List.of(tr(task, u1, 1)));

      // input with duplicates: [2,2,1,2,1] -> expected [2,1]
      TaskUpdateRequestDto dto = baseUpdateDto();
      dto.setResponsibleUserIds(List.of(2L, 2L, 1L, 2L, 1L));

      when(userRepo.findById(1L)).thenReturn(Optional.of(u1));
      when(userRepo.findById(2L)).thenReturn(Optional.of(u2));

      @SuppressWarnings("unchecked")
      ArgumentCaptor<List<TaskResponsible>> saveCap = ArgumentCaptor.forClass(List.class);

      // Act
      taskService.updateTask(400L, dto, email);

      // Assert: save changes for u2(pos1 new) and u1(pos2 moved)
      verify(respRepo).saveAll(saveCap.capture());
      List<TaskResponsible> saved = saveCap.getValue();

      java.util.Map<Long, Integer> posByUser = new java.util.HashMap<>();
      for (TaskResponsible r : saved) {
        posByUser.put(r.getUser().getUserId(), r.getPosition());
      }
      assertEquals(1, posByUser.get(2L)); // first unique
      assertEquals(2, posByUser.get(1L)); // second unique
    }

    @Test
    void shouldRejectEmptyResponsibleList() {
      // Arrange
      String email = "owner@example.com";
      Household h = hh(1L);

      User owner = user(10L, email, h);

      Task task = new Task();
      task.setTaskId(500L);
      task.setHousehold(h);
      task.setStartDate(LocalDate.now());

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(owner));
      when(taskRepo.findById(500L)).thenReturn(Optional.of(task));

      TaskUpdateRequestDto dto = baseUpdateDto();
      dto.setResponsibleUserIds(List.of()); // empty

      // Act + Assert
      assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(500L, dto, email));

      verify(respRepo, never()).deleteAll(any());
      verify(respRepo, never()).saveAll(any());
    }

    @Test
    void shouldRejectResponsibleFromDifferentHousehold() {
      // Arrange
      String email = "owner@example.com";
      Household h1 = hh(1L);
      Household h2 = hh(2L);

      User owner = user(10L, email, h1);
      User outsider = user(99L, "out@x", h2);

      Task task = new Task();
      task.setTaskId(600L);
      task.setHousehold(h1);
      task.setStartDate(LocalDate.now());

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(owner));
      when(taskRepo.findById(600L)).thenReturn(Optional.of(task));

      TaskUpdateRequestDto dto = baseUpdateDto();
      dto.setResponsibleUserIds(List.of(99L));

      when(userRepo.findById(99L)).thenReturn(Optional.of(outsider));

      // Act + Assert
      assertThrows(AccessDeniedException.class, () -> taskService.updateTask(600L, dto, email));

      verify(respRepo, never()).deleteAll(any());
      verify(respRepo, never()).saveAll(any());
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

      // Arrange stubs for new 2-step fetch
      when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
      when(taskRepo.findByHousehold_HouseholdIdOrderByNextDueAsc(1L)).thenReturn(List.of(task));

      // NEW: batch fetch for all responsibles for the returned tasks
      when(respRepo.findAllByTask_TaskIdInOrderByTask_TaskIdAscPositionAsc(List.of(11L)))
          .thenReturn(List.of(resp));

      // Act
      List<TaskResponseDto> result = taskService.getTasksForHousehold(email);

      // Assert DTO mapping
      assertEquals(1, result.size());
      TaskResponseDto dto = result.get(0);
      assertEquals("Clean kitchen", dto.getDescription());
      assertEquals(Frequency.DAILY, dto.getFrequency());
      assertEquals(1, dto.getResponsibles().size());
      assertEquals("Alice", dto.getResponsibles().get(0).getFullName());

      // Assert we used the batch method and NOT the per-task one
      verify(respRepo, times(1))
          .findAllByTask_TaskIdInOrderByTask_TaskIdAscPositionAsc(List.of(11L));
      verify(respRepo, never()).findAllByTask_TaskIdOrderByPositionAsc(anyLong());
    }

    @Test
    void shouldBatchFetchResponsiblesOnce_forAllTasks() {
      String email = "user@example.com";
      Household h = new Household(); h.setHouseholdId(1L);

      User u = new User(); u.setEmail(email); u.setHousehold(h);

      Task t1 = new Task(); t1.setTaskId(11L); t1.setHousehold(h);
      Task t2 = new Task(); t2.setTaskId(22L); t2.setHousehold(h);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(u));
      when(taskRepo.findByHousehold_HouseholdIdOrderByNextDueAsc(1L)).thenReturn(List.of(t1, t2));

      TaskResponsible r11 = new TaskResponsible(); r11.setTask(t1); r11.setUser(u); r11.setPosition(1);
      TaskResponsible r22 = new TaskResponsible(); r22.setTask(t2); r22.setUser(u); r22.setPosition(1);

      when(respRepo.findAllByTask_TaskIdInOrderByTask_TaskIdAscPositionAsc(List.of(11L, 22L)))
          .thenReturn(List.of(r11, r22));

      List<TaskResponseDto> out = taskService.getTasksForHousehold(email);

      assertEquals(2, out.size());
      verify(respRepo, times(1))
          .findAllByTask_TaskIdInOrderByTask_TaskIdAscPositionAsc(List.of(11L, 22L));
      verify(respRepo, never()).findAllByTask_TaskIdOrderByPositionAsc(anyLong());
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

  @Nested
  class GetTaskLogs {

    private Household hh() {
      Household h = new Household();
      h.setHouseholdId(1L);
      return h;
    }

    private User user(String email, Household h) {
      User u = new User();
      u.setUserId(10L);
      u.setEmail(email);
      u.setDisplayName("Alice");
      u.setHousehold(h);
      return u;
    }

    private Task task(long id, Household h, String desc) {
      Task t = new Task();
      t.setTaskId(id);
      t.setHousehold(h);
      t.setDescription(desc);
      t.setFrequency(Frequency.DAILY);
      t.setRotation(Rotation.SINGLE);
      t.setStartDate(LocalDate.now().minusDays(1));
      return t;
    }

    @Test
    void shouldReturnPagedHouseholdLogsOrderedByCompletedAtDesc() {
      String email = "user@example.com";
      Household h = hh();
      User u = user(email, h);

      Task t1 = task(101L, h, "Clean kitchen");
      Task t2 = task(102L, h, "Vacuum");

      // two logs (order in PageImpl content doesn't matter; sort is asserted via captured Pageable)
      TaskLog l1 = new TaskLog(t1, u);
      TaskLog l2 = new TaskLog(t2, u);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(u));

      // Return a page with two logs for whatever pageable we get
      when(logRepo.findByTask_Household_HouseholdId(eq(1L), any(Pageable.class)))
          .thenAnswer(inv -> new PageImpl<>(List.of(l1, l2)));

      // Act
      List<TaskLogResponseDto> out = taskService.getTaskLogs(email, 0, 20);

      // Assert mapping
      assertEquals(2, out.size());
      assertTrue(out.stream().anyMatch(d -> d.getTaskId().equals(101L) && "Clean kitchen".equals(d.getTaskDescription())));
      assertTrue(out.stream().anyMatch(d -> d.getTaskId().equals(102L) && "Vacuum".equals(d.getTaskDescription())));
      assertTrue(out.stream().allMatch(d -> "Alice".equals(d.getCompletedByFullName())));
      assertTrue(out.stream().allMatch(d -> d.getCompletedByUserId().equals(10L)));


      // Assert paging + sort used DESC on completedAt
      ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
      verify(logRepo).findByTask_Household_HouseholdId(eq(1L), cap.capture());
      Pageable used = cap.getValue();
      assertEquals(0, used.getPageNumber());
      assertEquals(20, used.getPageSize());
      Sort.Order order = used.getSort().getOrderFor("completedAt");
      assertNotNull(order, "Sort by completedAt should be applied");
      assertEquals(Sort.Direction.DESC, order.getDirection(), "completedAt must be DESC");
    }

    @Test
    void shouldClampPageNegativeToZero_andCapSizeTo100() {
      String email = "user@example.com";
      Household h = hh();
      User u = user(email, h);

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(u));
      when(logRepo.findByTask_Household_HouseholdId(eq(1L), any(Pageable.class)))
          .thenAnswer(inv -> new PageImpl<>(List.of())); // empty page ok

      // Act
      List<TaskLogResponseDto> out = taskService.getTaskLogs(email, -5, 1000);

      // Assert output (empty, fine)
      assertNotNull(out);
      assertTrue(out.isEmpty());

      // Assert clamped paging arguments
      ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
      verify(logRepo).findByTask_Household_HouseholdId(eq(1L), cap.capture());
      Pageable used = cap.getValue();
      assertEquals(0, used.getPageNumber(), "negative page should clamp to 0");
      assertEquals(100, used.getPageSize(), "size should cap to 100");
    }

    @Test
    void shouldThrowWhenUserHasNoHousehold() {
      String email = "user@example.com";
      User u = new User();
      u.setEmail(email);
      u.setDisplayName("Alice");
      u.setHousehold(null); // no household

      when(userRepo.findByEmail(email)).thenReturn(Optional.of(u));

      assertThrows(IllegalStateException.class, () -> taskService.getTaskLogs(email, 0, 20));
      verify(logRepo, never()).findByTask_Household_HouseholdId(anyLong(), any(Pageable.class));
    }
  }

}
