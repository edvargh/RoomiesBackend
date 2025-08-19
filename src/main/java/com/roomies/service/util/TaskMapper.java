package com.roomies.service.util;

import com.roomies.dto.TaskResponsibleDto;
import com.roomies.dto.TaskResponseDto;
import com.roomies.entity.Task;
import com.roomies.entity.TaskResponsible;

import java.util.List;

public final class TaskMapper {
  private TaskMapper() {}

  public static TaskResponseDto toDto(Task task, List<TaskResponsible> orderedResponsibles) {
    TaskResponseDto dto = new TaskResponseDto();
    dto.setTaskId(task.getTaskId());
    dto.setHouseholdId(task.getHousehold().getHouseholdId());
    dto.setDescription(task.getDescription());
    dto.setFrequency(task.getFrequency());
    dto.setRotation(task.getRotation());
    dto.setStartDate(task.getStartDate());
    dto.setNextDue(task.getNextDue());

    List<TaskResponsibleDto> rs = orderedResponsibles.stream()
        .map(r -> {
          TaskResponsibleDto rd = new TaskResponsibleDto();
          rd.setUserId(r.getUser().getUserId());
          rd.setFullName(r.getUser().getDisplayName());
          rd.setPosition(r.getPosition());
          return rd;
        })
        .toList();

    dto.setResponsibles(rs);
    return dto;
  }
}
