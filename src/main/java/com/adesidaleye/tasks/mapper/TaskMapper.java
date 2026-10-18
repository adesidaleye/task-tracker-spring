package com.adesidaleye.tasks.mapper;

import com.adesidaleye.tasks.domain.dto.TaskDto;
import com.adesidaleye.tasks.domain.entities.Task;

public interface TaskMapper {

    Task fromDto(TaskDto taskDto);

    TaskDto toDto(Task task);

}
