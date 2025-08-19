package com.roomies.service.util;

import com.roomies.entity.Frequency;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TaskSchedule {
  private TaskSchedule() {}

  public static LocalDateTime firstDue(LocalDate startDate) {
    return startDate.atStartOfDay();
  }

  public static LocalDateTime nextAfter(LocalDateTime from, Frequency f) {
    return switch (f) {
      case ONCE -> from;
      case DAILY -> from.plusDays(1);
      case EVERY_OTHER_DAY -> from.plusDays(2);
      case WEEKLY -> from.plusWeeks(1);
      case EVERY_OTHER_WEEK -> from.plusWeeks(2);
      case MONTHLY -> from.plusMonths(1);
    };
  }
}
